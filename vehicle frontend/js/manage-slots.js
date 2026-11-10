
const API_URL =
    "http://localhost:8081/vehicle_backend/ParkingControllerr";

const slotForm = document.getElementById("slotForm");
const formSection = document.getElementById("slotFormSection");

const parkingSelect = document.getElementById("parkingSelect");
const formParking = document.getElementById("formParking");

const vehicleFilter = document.getElementById("vehicleFilter");
const slotGrid = document.getElementById("slotGrid");
const slotTableBody = document.getElementById("slotTableBody");

let parkingList = [];
let slots = [];
let selectedSlotId = null;

// Read JSON safely
async function getResponseData(response) {
    const responseText = await response.text();

    console.log("HTTP STATUS:", response.status);
    console.log("SERVER RESPONSE:", responseText);

    try {
        return JSON.parse(responseText);
    } catch (error) {
        throw new Error("Backend returned HTML instead of JSON. Check Tomcat logs.");
    }
}

// Load parking locations from database
async function loadParkingLocations() {

    try {

        const response = await fetch(
            API_URL + "?parkingAction=view",
            {
                method: "GET",
                credentials: "include"
            }
        );

        const data = await getResponseData(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to load parking locations.");
        }

        parkingList = data.parkings || [];

        parkingSelect.innerHTML =
            '<option value="">Select Parking</option>';

        formParking.innerHTML =
            '<option value="">Select Parking</option>';

        parkingList.forEach(function(parking) {

            const option1 = document.createElement("option");

            option1.value = parking.parkingId;
            option1.textContent = parking.parkingName;

            const option2 = option1.cloneNode(true);

            parkingSelect.appendChild(option1);
            formParking.appendChild(option2);
        });

        if (parkingList.length > 0) {
            parkingSelect.value = parkingList[0].parkingId;
            formParking.value = parkingList[0].parkingId;

            await loadSlots();
        } else {
            displaySlots();
        }

    } catch (error) {

        console.error("Parking loading error:", error);

        slotGrid.innerHTML =
            '<p class="empty">Unable to load parking locations.</p>';
    }
}

// Load slots from MySQL
async function loadSlots() {

    const parkingId = parkingSelect.value;

    if (!parkingId) {
        slots = [];
        displaySlots();
        return;
    }

    try {

        const params = new URLSearchParams();

        params.append("slotAction", "view");
        params.append("parkingId", parkingId);

        const response = await fetch(
            API_URL + "?" + params.toString(),
            {
                method: "GET",
                credentials: "include"
            }
        );

        const data = await getResponseData(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to load slots.");
        }

        slots = data.slots || [];

        // Convert IDs to numbers for reliable comparisons
        slots = slots.map(function(slot) {
            return {
                ...slot,
                slotId: Number(slot.slotId),
                parkingId: Number(slot.parkingId)
            };
        });

        console.log("DATABASE SLOTS:", slots);

        displaySlots();

    } catch (error) {

        console.error("Slot loading error:", error);

        slotGrid.innerHTML =
            '<p class="empty">Unable to load parking slots.</p>';

        slotTableBody.innerHTML =
            '<tr><td colspan="6">Unable to load slots.</td></tr>';
    }
}

// Open add form
document.getElementById("openAddForm").addEventListener("click", function() {

    if (parkingList.length === 0) {
        alert("Please add a parking location first.");
        return;
    }

    clearForm();

    formSection.style.display = "block";

    formSection.scrollIntoView({ behavior: "smooth" });
});

// Cancel form
document.getElementById("cancelBtn").addEventListener("click", function() {

    clearForm();

    formSection.style.display = "none";
});

// Clear form
function clearForm() {

    slotForm.reset();

    document.getElementById("slotId").value = "";

    document.getElementById("formTitle").textContent =
        "Add New Parking Slot";

    document.getElementById("saveBtn").textContent =
        "Save Slot";

    document.getElementById("message").textContent = "";

    formParking.value = parkingSelect.value;
}

// Show message
function showMessage(message, color) {

    const msg = document.getElementById("message");

    msg.textContent = message;
    msg.style.color = color;
}

// Add or update slot in database
slotForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const id = document.getElementById("slotId").value;

    const parkingId = Number(formParking.value);

    const slotNumber = document.getElementById("slotNumber")
        .value.trim().toUpperCase();

    const vehicleType = document.getElementById("vehicleType").value;

    const status = document.getElementById("slotStatus").value;

    if (!parkingId) {
        showMessage("Please select a parking location.", "red");
        return;
    }

    if (!slotNumber) {
        showMessage("Please enter a slot number.", "red");
        return;
    }

    if (!/^[A-Z0-9-]{1,20}$/.test(slotNumber)) {
        showMessage("Use only letters, numbers and hyphens.", "red");
        return;
    }

    if (!vehicleType) {
        showMessage("Please select vehicle type.", "red");
        return;
    }

    const duplicate = slots.some(function(s) {

        return Number(s.parkingId) === parkingId &&
               s.slotNumber.toUpperCase() === slotNumber &&
               Number(s.slotId) !== Number(id);
    });

    if (duplicate) {
        showMessage("This slot number already exists.", "red");
        return;
    }

    const formData = new URLSearchParams();

    formData.append("slotAction", id ? "update" : "add");

    formData.append("parkingId", parkingId);
    formData.append("slotNumber", slotNumber);
    formData.append("vehicleType", vehicleType);

    if (id) {
        formData.append("slotId", id);
        formData.append("status", status);
    }

    try {

        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        const data = await getResponseData(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Slot operation failed.");
        }

        showMessage(data.message || "Slot saved successfully.", "green");

        // Fetch newly saved records from MySQL
        await loadSlots();

        clearForm();

        setTimeout(function() {
            formSection.style.display = "none";
        }, 700);

    } catch (error) {

        console.error("Slot saving error:", error);

        showMessage(error.message, "red");
    }
});


// Display slots as colorful parking boxes
function displaySlots() {

    const parkingId = Number(parkingSelect.value);
    const filter = vehicleFilter.value;

    const filteredSlots = slots.filter(function(slot) {
        return Number(slot.parkingId) === parkingId &&
               (filter === "ALL" || slot.vehicleType === filter);
    });

    let total = 0;
    let available = 0;
    let booked = 0;
    let maintenance = 0;

    filteredSlots.forEach(function(slot) {

        total++;

        const status = (slot.status || "").toUpperCase();

        if (status === "AVAILABLE") available++;
        if (status === "BOOKED") booked++;
        if (status === "MAINTENANCE") maintenance++;
    });

    document.getElementById("totalSlots").textContent = total;
    document.getElementById("availableSlots").textContent = available;
    document.getElementById("bookedSlots").textContent = booked;

    // Display maintenance count if this element exists
    const maintenanceCount = document.getElementById("maintenanceSlots");
    if (maintenanceCount) {
        maintenanceCount.textContent = maintenance;
    }

    // Clear previous parking boxes
    slotGrid.innerHTML = "";

    if (filteredSlots.length === 0) {
        slotGrid.innerHTML =
            '<p class="empty">No slots found. Please add parking slots.</p>';
        return;
    }

    filteredSlots.forEach(function(slot) {

        const status = (slot.status || "AVAILABLE").toUpperCase();
        const statusClass = status.toLowerCase();

        // Create parking box
        const div = document.createElement("div");

        div.className = "slot-card " + statusClass;

        if (selectedSlotId === slot.slotId) {
            div.classList.add("selected");
        }

        // Parking icon
        const icon = document.createElement("div");
        icon.className = "slot-icon";
        icon.textContent =
            slot.vehicleType === "BIKE" ? "🏍️" : "🚗";

        // Slot number
        const name = document.createElement("h3");
        name.textContent = slot.slotNumber;

        // Vehicle type
        const type = document.createElement("p");
        type.textContent = slot.vehicleType;

        // Status
        const statusText = document.createElement("span");
        statusText.className = "slot-status";
        statusText.textContent = status;

        div.append(icon, name, type, statusText);

        // Select available slot
        div.addEventListener("click", function() {
            if (status === "AVAILABLE") {
                selectSlot(slot.slotId);
            }
        });

        // Edit button
        const editButton = document.createElement("button");
        editButton.className = "slot-edit-btn";
        editButton.textContent = "Edit";

        editButton.addEventListener("click", function(event) {
            event.stopPropagation();
            editSlot(slot.slotId);
        });

        div.appendChild(editButton);

        // Delete button (only available slots)
        if (status === "AVAILABLE") {

            const deleteButton = document.createElement("button");
            deleteButton.className = "slot-delete-btn";
            deleteButton.textContent = "Delete";

            deleteButton.addEventListener("click", function(event) {
                event.stopPropagation();
                deleteSlot(slot.slotId, slot.parkingId);
            });

            div.appendChild(deleteButton);
        }

        slotGrid.appendChild(div);
    });

    // Hide old table because we are using visual parking boxes
    const oldTable = slotTableBody.closest("table");
    if (oldTable) {
        oldTable.style.display = "none";
    }
}

// Display slot table
function displayTable(list) {

    slotTableBody.innerHTML = "";

    if (list.length === 0) {

        slotTableBody.innerHTML =
            '<tr><td colspan="6" class="empty">No slot records available.</td></tr>';

        return;
    }

    list.forEach(function(slot) {

        const parking = parkingList.find(function(p) {
            return Number(p.parkingId) === Number(slot.parkingId);
        });

        const row = document.createElement("tr");

        const values = [
            slot.slotId,
            parking ? parking.parkingName : "Unknown",
            slot.slotNumber,
            slot.vehicleType
        ];

        values.forEach(function(value) {

            const td = document.createElement("td");

            td.textContent = value;

            row.appendChild(td);
        });

        const statusCell = document.createElement("td");

        const status = document.createElement("span");

        status.className = "table-status " + slot.status;
        status.textContent = slot.status;

        statusCell.appendChild(status);

        row.appendChild(statusCell);

        const actionCell = document.createElement("td");

        const edit = document.createElement("button");

        edit.className = "action-btn edit-btn";
        edit.textContent = "Edit";

        edit.addEventListener("click", function() {
            editSlot(slot.slotId);
        });

        const remove = document.createElement("button");

        remove.className = "action-btn delete-btn";
        remove.textContent = "Delete";

        remove.addEventListener("click", function() {
            deleteSlot(slot.slotId, slot.parkingId);
        });

        actionCell.append(edit, remove);

        row.appendChild(actionCell);

        slotTableBody.appendChild(row);
    });
}

// Edit slot
function editSlot(id) {

    const slot = slots.find(function(s) {
        return Number(s.slotId) === Number(id);
    });

    if (!slot) {
        return;
    }

    document.getElementById("slotId").value = slot.slotId;

    formParking.value = slot.parkingId;

    document.getElementById("slotNumber").value =
        slot.slotNumber;

    document.getElementById("vehicleType").value =
        slot.vehicleType;

    document.getElementById("slotStatus").value =
        slot.status;

    document.getElementById("formTitle").textContent =
        "Update Parking Slot";

    document.getElementById("saveBtn").textContent =
        "Update Slot";

    formSection.style.display = "block";

    formSection.scrollIntoView({ behavior: "smooth" });
}

// Delete slot from database
async function deleteSlot(id, parkingId) {

    const confirmDelete = confirm(
        "Are you sure you want to delete this slot?"
    );

    if (!confirmDelete) {
        return;
    }

    const formData = new URLSearchParams();

    formData.append("slotAction", "delete");
    formData.append("slotId", id);
    formData.append("parkingId", parkingId);

    try {

        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        const data = await getResponseData(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to delete slot.");
        }

        alert(data.message || "Slot deleted successfully.");

        selectedSlotId = null;

        await loadSlots();

    } catch (error) {

        console.error("Slot delete error:", error);

        alert(error.message);
    }
}

// Parking location filter
parkingSelect.addEventListener("change", async function() {

    selectedSlotId = null;

    await loadSlots();
});

// Vehicle type filter
vehicleFilter.addEventListener("change", function() {

    selectedSlotId = null;

    displaySlots();
});

// Initialize page
loadParkingLocations();
