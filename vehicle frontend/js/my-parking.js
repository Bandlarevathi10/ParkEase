
const API_URL =
    "http://localhost:8081/vehicle_backend/ParkingControllerr";

const parkingForm = document.getElementById("parkingForm");
const formSection = document.getElementById("parkingFormSection");
const parkingList = document.getElementById("parkingList");

const openFormBtn = document.getElementById("openFormBtn");
const cancelBtn = document.getElementById("cancelBtn");
const searchParking = document.getElementById("searchParking");

let parkingData = [];

// Convert response text into JSON safely
async function getJsonResponse(response) {
    const text = await response.text();

    console.log("HTTP STATUS:", response.status);
    console.log("SERVER RESPONSE:", text);

    try {
        return JSON.parse(text);
    } catch (error) {
        throw new Error("Backend returned HTML instead of JSON. Check Tomcat logs.");
    }
}

// Load parking locations
async function loadParking() {
    try {
        const response = await fetch(
            API_URL + "?parkingAction=view",
            {
                method: "GET",
                credentials: "include"
            }
        );

        const data = await getJsonResponse(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to load parking.");
        }

        // IMPORTANT: Store backend records in parkingData
        parkingData = data.parkings || [];

console.log("PARKING RECORDS:", parkingData);

displayParking();

    } catch (error) {
        console.error("Parking loading error:", error);

        parkingList.innerHTML =
            '<div class="empty-message">Unable to load parking locations.</div>';
    }
}

// Show form
function showForm() {
    formSection.style.display = "block";
    formSection.scrollIntoView({ behavior: "smooth" });
}

function clearForm() {
    parkingForm.reset();

    document.getElementById("parkingId").value = "";

    document.getElementById("formTitle").textContent =
        "Add New Parking Location";

    document.getElementById("submitBtn").textContent =
        "Save Parking";

    document.getElementById("message").textContent = "";

    // Enable slot count fields for adding a new parking location
    carSlotsInput.disabled = false;
    bikeSlotsInput.disabled = false;

    calculateTotalSlots();
}

// Open form
openFormBtn.addEventListener("click", function() {
    clearForm();
    showForm();
});

// Cancel form
cancelBtn.addEventListener("click", function() {
    clearForm();
    formSection.style.display = "none";
});

// Show message
function showMessage(message, color) {
    const msg = document.getElementById("message");
    msg.textContent = message;
    msg.style.color = color;
}

parkingForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const id = document.getElementById("parkingId").value;

    const carSlots = Number(carSlotsInput.value);
    const bikeSlots = Number(bikeSlotsInput.value);

    const totalSlots = carSlots + bikeSlots;

    const parking = {
        parkingName: document.getElementById("parkingName").value.trim(),
        city: document.getElementById("city").value.trim(),
        location: document.getElementById("location").value.trim(),
        address: document.getElementById("address").value.trim(),
        totalSlots: id
            ? Number(totalSlotsInput.value)
            : totalSlots,
        pricePerHour: Number(document.getElementById("price").value),
        status: document.getElementById("status").value,
        description: document.getElementById("description").value.trim()
    };

    // Validate parking details
    if (
        parking.parkingName.length < 3 ||
        parking.city.length < 2 ||
        parking.location.length < 2
    ) {
        showMessage("Please enter valid parking details.", "red");
        return;
    }

    // Validate slot counts only when adding new parking
    if (!id) {

        if (
            !Number.isInteger(carSlots) ||
            !Number.isInteger(bikeSlots) ||
            carSlots < 0 ||
            bikeSlots < 0 ||
            totalSlots <= 0
        ) {
            showMessage(
                "Enter valid car and bike slot counts. Total must be greater than zero.",
                "red"
            );
            return;
        }
    }

    // Validate price
    if (
        !Number.isFinite(parking.pricePerHour) ||
        parking.pricePerHour <= 0
    ) {
        showMessage("Price must be greater than zero.", "red");
        return;
    }

    const formData = new URLSearchParams();

    formData.append("parkingAction", id ? "update" : "add");

    if (id) {
        formData.append("parkingId", id);
    }

    Object.keys(parking).forEach(function(key) {
        formData.append(key, parking[key]);
    });

    
if (!id) {
    formData.append(
        "carSlots",
        document.getElementById("carSlots").value
    );

    formData.append(
        "bikeSlots",
        document.getElementById("bikeSlots").value
    );
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

        const data = await getJsonResponse(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Parking operation failed.");
        }

        showMessage(
            data.message || "Parking saved successfully.",
            "green"
        );

        await loadParking();

        clearForm();

        setTimeout(function() {
            formSection.style.display = "none";
        }, 800);

    } catch (error) {

        console.error("Parking save error:", error);

        showMessage(error.message, "red");
    }
});

// Display parking cards
function displayParking(list = parkingData) {

    parkingList.innerHTML = "";

    let activeCount = 0;
    let slotCount = 0;

    parkingData.forEach(function(p) {

        if (p.status === "ACTIVE") {
            activeCount++;
        }

        slotCount += Number(p.totalSlots) || 0;
    });

    document.getElementById("totalParking").textContent =
        parkingData.length;

    document.getElementById("activeParking").textContent =
        activeCount;

    document.getElementById("totalSlots").textContent =
        slotCount;

    if (list.length === 0) {
        parkingList.innerHTML =
            '<div class="empty-message">No parking locations found.</div>';
        return;
    }

    list.forEach(function(p) {

        const card = document.createElement("div");
        card.className = "parking-card";

        const top = document.createElement("div");
        top.className = "card-top";

        const title = document.createElement("h3");
        title.textContent = p.parkingName;

        const status = document.createElement("span");
        status.className = "status " + p.status;
        status.textContent = p.status;

        top.append(title, status);

        const location = document.createElement("p");
        location.textContent =
            p.location + ", " + p.city + " | " +
            (p.address || "Address not added");

        const description = document.createElement("p");
        description.textContent =
            p.description || "No description available.";

        const details = document.createElement("div");
        details.className = "card-details";

        const slotInfo = document.createElement("div");
        slotInfo.innerHTML = "<span>Total Slots</span><strong></strong>";
        slotInfo.querySelector("strong").textContent = p.totalSlots;

        const priceInfo = document.createElement("div");
        priceInfo.innerHTML =
            "<span>Price Per Hour</span><strong></strong>";

        priceInfo.querySelector("strong").textContent =
            "₹" + Number(p.pricePerHour).toFixed(2);

        details.append(slotInfo, priceInfo);

        const actions = document.createElement("div");
        actions.className = "card-actions";

        const editButton = document.createElement("button");
        editButton.className = "edit-btn";
        editButton.textContent = "Edit";

        editButton.addEventListener("click", function() {
            editParking(p.parkingId);
        });

        const deleteButton = document.createElement("button");
        deleteButton.className = "delete-btn";
        deleteButton.textContent = "Delete";

        deleteButton.addEventListener("click", function() {
            deleteParking(p.parkingId);
        });

        actions.append(editButton, deleteButton);

        card.append(top, location, description, details, actions);

        parkingList.appendChild(card);
    });
}

// Edit parking
function editParking(id) {
    
    const parking = parkingData.find(function(p) {
        return Number(p.parkingId) === Number(id);
    });

    if (!parking) {
        return;
    }

    document.getElementById("parkingId").value = parking.parkingId;
    document.getElementById("parkingName").value = parking.parkingName;
    document.getElementById("city").value = parking.city;
    document.getElementById("location").value = parking.location;
    document.getElementById("address").value = parking.address || "";

    document.getElementById("totalSlotsInput").value =
        parking.totalSlots;
    // Do not regenerate slots when editing parking details
carSlotsInput.disabled = true;
bikeSlotsInput.disabled = true;
    document.getElementById("price").value =
        parking.pricePerHour;

    document.getElementById("status").value = parking.status;

    document.getElementById("description").value =
        parking.description || "";

    document.getElementById("formTitle").textContent =
        "Update Parking Location";

    document.getElementById("submitBtn").textContent =
        "Update Parking";

    showForm();
}

// Delete parking
async function deleteParking(id) {

    const confirmDelete = confirm(
        "Are you sure you want to delete this parking location?"
    );

    if (!confirmDelete) {
        return;
    }

    const formData = new URLSearchParams();

    formData.append("parkingAction", "delete");
    formData.append("parkingId", id);

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        const data = await getJsonResponse(response);

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to delete parking.");
        }

        alert(data.message || "Parking deleted successfully.");

        await loadParking();

    } catch (error) {
        console.error("Parking delete error:", error);
        alert(error.message);
    }
}

// Search parking
searchParking.addEventListener("input", function() {

    const value = searchParking.value.toLowerCase();

    const filtered = parkingData.filter(function(p) {

        return (
            (p.parkingName || "").toLowerCase().includes(value) ||
            (p.city || "").toLowerCase().includes(value) ||
            (p.location || "").toLowerCase().includes(value)
        );
    });

    displayParking(filtered);
});

// Initialize page
loadParking();
const carSlotsInput = document.getElementById("carSlots");
const bikeSlotsInput = document.getElementById("bikeSlots");
const totalSlotsInput = document.getElementById("totalSlotsInput");

function calculateTotalSlots() {
    const cars = parseInt(carSlotsInput.value) || 0;
    const bikes = parseInt(bikeSlotsInput.value) || 0;

    totalSlotsInput.value = cars + bikes;
}

carSlotsInput.addEventListener("input", calculateTotalSlots);
bikeSlotsInput.addEventListener("input", calculateTotalSlots);
