
document.addEventListener("DOMContentLoaded", function () {

    const slotGrid = document.getElementById("slotGrid");
    const vehicleType = document.getElementById("vehicleType");
    const slotDate = document.getElementById("slotDate");
    const startTime = document.getElementById("startTime");
    const endTime = document.getElementById("endTime");
    const selectedSlot = document.getElementById("selectedSlot");
    const selectionMessage = document.getElementById("selectionMessage");
    const continueBtn = document.getElementById("continueBtn");

    // Get the actual parking ID selected on Find Parking page
    const parkingId = sessionStorage.getItem("selectedParkingId");

    let selectedSlotId = null;
    let selectedSlotNumber = null;

    if (!slotGrid || !vehicleType || !continueBtn) {
        console.error("Required HTML elements are missing.");
        return;
    }

    // Check selected parking
    if (!parkingId) {
        alert("Please select a parking location first.");
        window.location.href = "findparking.html";
        return;
    }

    console.log("Selected Parking ID:", parkingId);

    // Load slots when vehicle type changes
    vehicleType.addEventListener("change", loadSlots);

    function loadSlots() {

        selectedSlotId = null;
        selectedSlotNumber = null;

        selectedSlot.textContent = "Not selected";
        selectionMessage.textContent = "Please select an available slot.";
        continueBtn.disabled = true;

        const type = vehicleType.value;

        if (!type) {
            slotGrid.innerHTML = "<p>Please select CAR or BIKE.</p>";
            return;
        }

        slotGrid.innerHTML = "<p>Loading parking slots...</p>";

       const url =
    "http://localhost:8081/vehicle_backend/SlotControllerr";

        const formData = new URLSearchParams();

        formData.append("action", "Slots");
        formData.append("slotAction", "view");
        formData.append("parkingId", parkingId);
        formData.append("vehicleType", type);

        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            credentials: "include",
            body: formData.toString()
        })

        .then(response => {

            if (!response.ok) {
                throw new Error("Server error: " + response.status);
            }

            return response.json();
        })

        .then(data => {

            console.log("Slots response:", data);

            if (!data.success) {
                throw new Error(data.message || "Unable to load slots.");
            }

            slotGrid.innerHTML = "";

            if (!data.slots || data.slots.length === 0) {
                slotGrid.innerHTML =
                    "<p>No " + type + " slots available for this parking.</p>";
                return;
            }

            data.slots.forEach(function (slot) {

                const button = document.createElement("button");

                button.type = "button";
                button.setAttribute("data-slot-id", slot.slotId);
                button.setAttribute("data-slot-number", slot.slotNumber);

                button.textContent = slot.slotNumber;

                if (slot.status.toUpperCase() === "AVAILABLE") {

                    button.className = "slot available";

                } else {

                    button.className = "slot booked";
                    button.disabled = true;
                }

                slotGrid.appendChild(button);

            });

            console.log("Total slots loaded:", data.slots.length);

        })

        .catch(error => {

            console.error("Slot loading error:", error);

            slotGrid.innerHTML =
                "<p>" + error.message + "</p>";

            selectionMessage.textContent = "Unable to load slots.";

        });
    }

    // Select a parking slot
    slotGrid.addEventListener("click", function (event) {

        const button = event.target.closest("button[data-slot-id]");

        if (!button || button.disabled) {
            return;
        }

        selectedSlotId = button.getAttribute("data-slot-id");
        selectedSlotNumber = button.getAttribute("data-slot-number");

        if (!selectedSlotId || !/^\d+$/.test(selectedSlotId)) {
            alert("Invalid slot ID. Please refresh the page.");
            return;
        }

        slotGrid.querySelectorAll("button[data-slot-id]")
            .forEach(btn => btn.classList.remove("selected"));

        button.classList.add("selected");

        selectedSlot.textContent = selectedSlotNumber;
        selectionMessage.textContent = "Slot selected successfully.";

        continueBtn.disabled = false;

        console.log("Selected Slot ID:", selectedSlotId);
        console.log("Selected Slot Number:", selectedSlotNumber);

    });

    // Continue to booking
    continueBtn.addEventListener("click", function () {

        if (!selectedSlotId) {
            alert("Please select a parking slot.");
            return;
        }

        if (!slotDate.value || !startTime.value || !endTime.value) {
            alert("Please select date, start time and end time.");
            return;
        }

        if (endTime.value <= startTime.value) {
            alert("End time must be later than start time.");
            return;
        }

        sessionStorage.setItem("selectedSlotId", selectedSlotId);
        sessionStorage.setItem("selectedSlotNumber", selectedSlotNumber);
        sessionStorage.setItem("parkingId", parkingId);
        sessionStorage.setItem("selectedParkingId", parkingId);
        sessionStorage.setItem("vehicleType", vehicleType.value);
        sessionStorage.setItem("parkingDate", slotDate.value);
        sessionStorage.setItem("startTime", startTime.value);
        sessionStorage.setItem("endTime", endTime.value);

        window.location.href = "booking.html";

    });

});
