
document.addEventListener("DOMContentLoaded", function () {

    // Get values saved from parkingslots.js
    const parkingId = sessionStorage.getItem("parkingId");
    const slotNumber = sessionStorage.getItem("selectedSlotNumber");
    const slotId = sessionStorage.getItem("selectedSlotId");

    const vehicleType = (
        sessionStorage.getItem("vehicleType") || ""
    ).toUpperCase();

    const parkingDate = sessionStorage.getItem("parkingDate");
    const startTime = sessionStorage.getItem("startTime");
    const endTime = sessionStorage.getItem("endTime");

    const parkingDetails = {
        1: { name: "City Center Parking", location: "Brodipet, Guntur", price: 30 },
        2: { name: "Green Park Parking", location: "Lakshmipuram, Guntur", price: 40 },
        3: { name: "Metro Parking Zone", location: "Benz Circle, Vijayawada", price: 50 },
        4: { name: "Royal Parking Plaza", location: "Arundelpet, Guntur", price: 35 },
        5: { name: "City Mall Parking", location: "MG Road, Vijayawada", price: 40 },
        6: { name: "Metro Smart Parking", location: "Ameerpet, Hyderabad", price: 50 },
        7: { name: "Tech Park Parking", location: "Kukatpally, Hyderabad", price: 40 },
        8: { name: "Cyber Towers Parking", location: "Madhapur, Hyderabad", price: 60 }
    };

    const parking = parkingDetails[parkingId];

    // Display parking information
    if (parking) {
        document.getElementById("parkingName").textContent = parking.name;
        document.getElementById("parkingLocation").textContent = parking.location;
        document.getElementById("pricePerHour").textContent = "₹" + parking.price;
    }

    document.getElementById("parkingId").textContent = parkingId || "-";
    document.getElementById("slotNumber").textContent = slotNumber || "-";
    document.getElementById("vehicleType").value = vehicleType;
    document.getElementById("summaryVehicle").textContent = vehicleType || "-";
    document.getElementById("parkingDate").textContent = parkingDate || "-";
    document.getElementById("startTime").textContent = startTime || "-";
    document.getElementById("endTime").textContent = endTime || "-";

    // Calculate duration
    let duration = 0;

    if (startTime && endTime) {
        const start = startTime.split(":");
        const end = endTime.split(":");

        const startMinutes = Number(start[0]) * 60 + Number(start[1]);
        const endMinutes = Number(end[0]) * 60 + Number(end[1]);

        duration = (endMinutes - startMinutes) / 60;
    }

    document.getElementById("duration").textContent =
        duration > 0 ? duration.toFixed(2) + " hours" : "-";

    // Display estimated charges
    const price = parking ? parking.price : 0;
    const total = duration > 0 ? duration * price : 0;

    document.getElementById("parkingCharges").textContent =
        "₹" + total.toFixed(2);

    document.getElementById("totalAmount").textContent =
        "₹" + total.toFixed(2);

    // Get booking form
    const bookingForm = document.getElementById("bookingForm");

    // Set hidden fields immediately
    document.getElementById("formParkingId").value = parkingId || "";
    document.getElementById("formSlotId").value = slotId || "";
    document.getElementById("formParkingDate").value = parkingDate || "";
    document.getElementById("formStartTime").value = startTime || "";
    document.getElementById("formEndTime").value = endTime || "";

    // Check saved values
    console.log("Parking ID:", parkingId);
    console.log("Slot Number:", slotNumber);
    console.log("Slot ID:", slotId);
    console.log("Vehicle Type:", vehicleType);

    bookingForm.addEventListener("submit", function (event) {

        const vehicleNumber =
            document.getElementById("vehicleNumber").value.trim();

        const vehicleModel =
            document.getElementById("vehicleModel").value.trim();

        const terms =
            document.getElementById("terms").checked;

        const message = document.getElementById("message");

        if (!parkingId || !slotId || !/^\d+$/.test(slotId) ||
            !slotNumber || !vehicleType || !parkingDate ||
            !startTime || !endTime) {

            event.preventDefault();

            message.textContent =
                "Parking details are missing. Please select your slot again.";

            message.style.color = "red";
            return;
        }

        if (!vehicleNumber || !vehicleModel || !terms) {
            event.preventDefault();

            message.textContent =
                "Please complete all required details.";

            message.style.color = "red";
            return;
        }

        if (duration <= 0) {
            event.preventDefault();

            message.textContent =
                "Please select a valid parking duration.";

            message.style.color = "red";
            return;
        }

        // Update hidden fields before form submission
        document.getElementById("formParkingId").value = parkingId;
        document.getElementById("formSlotId").value = slotId;
        document.getElementById("formParkingDate").value = parkingDate;
        document.getElementById("formStartTime").value = startTime;
        document.getElementById("formEndTime").value = endTime;

        console.log("Submitting Slot ID:", slotId);
    });

});
