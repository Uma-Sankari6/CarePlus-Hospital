
package com.medicore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/api/appointments")
public class AppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        Connection connection = null;

        try {
            // Read JSON request
            StringBuilder data = new StringBuilder();

            BufferedReader reader = request.getReader();
            String line;

            while ((line = reader.readLine()) != null) {
                data.append(line);
            }

            JSONObject json = new JSONObject(data.toString());

            // Get patient details
            String patientName = json.getString("patient_name");
            String email = json.getString("email");
            String phone = json.getString("phone");

            int doctorId = json.getInt("doctor_id");

            String appointmentDate =
                    json.getString("appointment_date");

            // Validate input
            if (patientName.trim().isEmpty()
                    || email.trim().isEmpty()
                    || phone.trim().length() != 10) {

                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
                );

                out.print("{\"error\":\"Invalid patient details\"}");
                return;
            }

            // Read database settings
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            if (dbUrl == null || dbUser == null
                    || dbPassword == null) {
                throw new Exception(
                    "Database configuration is missing"
                );
            }

            // Connect to MySQL
            connection = DriverManager.getConnection(
                dbUrl, dbUser, dbPassword
            );

            connection.setAutoCommit(false);

            // Check doctor exists
            String doctorSql =
                "SELECT doctor_id FROM doctors WHERE doctor_id = ?";

            try (PreparedStatement doctorStmt =
                    connection.prepareStatement(doctorSql)) {

                doctorStmt.setInt(1, doctorId);

                try (ResultSet doctorResult =
                        doctorStmt.executeQuery()) {

                    if (!doctorResult.next()) {
                        throw new Exception("Doctor not found");
                    }
                }
            }

            // Insert patient
            String patientSql =
                "INSERT INTO patients " +
                "(patient_name, email, phone) VALUES (?, ?, ?)";

            int patientId;

            try (PreparedStatement patientStmt =
                    connection.prepareStatement(
                        patientSql,
                        java.sql.Statement.RETURN_GENERATED_KEYS
                    )) {

                patientStmt.setString(1, patientName);
                patientStmt.setString(2, email);
                patientStmt.setString(3, phone);

                patientStmt.executeUpdate();

                try (ResultSet keys =
                        patientStmt.getGeneratedKeys()) {

                    if (!keys.next()) {
                        throw new Exception(
                            "Patient ID not generated"
                        );
                    }

                    patientId = keys.getInt(1);
                }
            }

            // Insert appointment
            String appointmentSql =
                "INSERT INTO appointments " +
                "(patient_id, doctor_id, appointment_date) " +
                "VALUES (?, ?, ?)";

            try (PreparedStatement appointmentStmt =
                    connection.prepareStatement(appointmentSql)) {

                appointmentStmt.setInt(1, patientId);
                appointmentStmt.setInt(2, doctorId);

                appointmentStmt.setDate(
                    3, Date.valueOf(appointmentDate)
                );

                appointmentStmt.executeUpdate();
            }

            // Save both inserts
            connection.commit();

            response.setStatus(
                HttpServletResponse.SC_CREATED
            );

            out.print(
                "{\"message\":\"Appointment booked successfully!\"}"
            );

        } catch (Exception e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                "{\"error\":\"Appointment booking failed\"}"
            );

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
