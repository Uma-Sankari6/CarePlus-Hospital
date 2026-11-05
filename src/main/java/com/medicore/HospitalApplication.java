package com.medicore;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class HospitalApplication extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>MediCore Hospital</title></head>");
        out.println("<body style='text-align:center;font-family:Arial;'>");
        out.println("<h1>Welcome to MediCore Hospital</h1>");
        out.println("<h2>Hospital Management System</h2>");
        out.println("<p>Java Application Running Successfully!</p>");
        out.println("</body>");
        out.println("</html>");
    }
}