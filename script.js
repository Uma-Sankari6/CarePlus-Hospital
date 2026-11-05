
document.addEventListener("DOMContentLoaded", function () {

    const form = document.querySelector("#appointmentForm");

    if (!form) {
        console.log("Appointment form not found");
        return;
    }

    const nameInput = form.querySelector('input[type="text"]');
    const emailInput = form.querySelector('input[type="email"]');
    const phoneInput = form.querySelector('input[type="tel"]');
    const dateInput = form.querySelector('input[type="date"]');
    const departmentInput = form.querySelector("select");

    // Get today's local date
    function getTodayDate() {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, "0");
        const day = String(today.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    }

    // Prevent past dates
    const todayDate = getTodayDate();
    dateInput.min = todayDate;

    // Allow only 10 digit phone numbers
    phoneInput.addEventListener("input", function () {
        this.value = this.value.replace(/\D/g, "").slice(0, 10);
    });

    // Department and doctor mapping
    const doctorMap = {
        "Cardiology": 1,
        "Physiotherapy": 2,
        "General Medicine": 3,
        "Pediatrics": 4,
        "Dental Care": 5
    };

    // Appointment form submission
    form.addEventListener("submit", async function (event) {

        event.preventDefault();

        const patientName = nameInput.value.trim();
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim();
        const appointmentDate = dateInput.value;
        const department = departmentInput.value;

        // Validate fields
        if (!patientName || !email || !phone ||
            !appointmentDate || !department) {
            alert("Please fill all fields!");
            return;
        }

        // Validate phone
        if (!/^\d{10}$/.test(phone)) {
            alert("Please enter a valid 10-digit phone number!");
            return;
        }

        // Validate appointment date
        if (appointmentDate < getTodayDate()) {
            alert("Past dates are not allowed!");
            return;
        }

        const doctorId = doctorMap[department];

        if (!doctorId) {
            alert("Please select a valid department!");
            return;
        }

        // Prepare appointment data
        const appointmentData = {
            patient_name: patientName,
            email: email,
            phone: phone,
            doctor_id: doctorId,
            appointment_date: appointmentDate
        };

        try {

            const response = await fetch("/api/appointments", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(appointmentData)
            });

            const result = await response.json();

            if (response.ok) {

                alert("Appointment booked successfully!");

                form.reset();

                dateInput.min = getTodayDate();

            } else {

                alert("Booking failed: " + result.error);

            }

        } catch (error) {

            console.error(error);

            alert("Unable to connect to the server!");

        }

    });

});
