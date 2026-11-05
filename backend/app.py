
from flask import Flask, request, jsonify, send_from_directory
import mysql.connector
import os

app = Flask(__name__)

PROJECT_FOLDER = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def get_db_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="uma",
        database="careplus_hospital_db"
    )

@app.route("/")
def home():
    return send_from_directory(PROJECT_FOLDER, "index.html")

@app.route("/<path:filename>")
def website_files(filename):
    return send_from_directory(PROJECT_FOLDER, filename)

@app.route("/api/appointments", methods=["POST"])
def add_appointment():
    data = request.get_json()

    db = get_db_connection()
    cursor = db.cursor()

    try:
        cursor.execute(
            "INSERT INTO patients (patient_name, email, phone) VALUES (%s, %s, %s)",
            (data["patient_name"], data["email"], data["phone"])
        )

        patient_id = cursor.lastrowid

        cursor.execute(
            "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (%s, %s, %s)",
            (patient_id, data["doctor_id"], data["appointment_date"])
        )

        db.commit()
        return jsonify({"message": "Appointment saved successfully!"}), 201

    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 500

    finally:
        cursor.close()
        db.close()

if __name__ == "__main__":
    app.run(debug=True)
