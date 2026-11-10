
const vehicleForm = document.getElementById("vehicleForm");
const vehicleMessage = document.getElementById("vehicleMessage");


// Get saved vehicles
function getVehicles() {

    return JSON.parse(
        sessionStorage.getItem("vehicleList")
    ) || [];

}


// Save vehicles
function saveVehicles(vehicles) {

    sessionStorage.setItem(
        "vehicleList",
        JSON.stringify(vehicles)
    );

}


// Display registered vehicles
function displayVehicles() {

    const vehicles = getVehicles();

    const vehicleList = document.getElementById("vehicleList");

    const vehicleCount = document.getElementById("vehicleCount");

    vehicleList.innerHTML = "";

    vehicleCount.textContent = vehicles.length + " Vehicles";

    if (vehicles.length === 0) {

        vehicleList.innerHTML = `
            <div class="empty-state">
                No vehicles registered yet.
            </div>
        `;

        return;

    }

    vehicles.forEach(function(vehicle) {

        const card = document.createElement("div");

        card.className = "vehicle-card";

        const details = document.createElement("div");

        const heading = document.createElement("h3");
        heading.textContent = vehicle.vehicleNumber;

        const type = document.createElement("span");
        type.className = "vehicle-type";
        type.textContent = vehicle.vehicleType;

        const model = document.createElement("p");
        model.textContent = "Model: " + vehicle.vehicleModel;

        const color = document.createElement("p");
        color.textContent = "Color: " + vehicle.vehicleColor;

        details.appendChild(heading);
        details.appendChild(type);
        details.appendChild(model);
        details.appendChild(color);

        const deleteButton = document.createElement("button");

        deleteButton.className = "delete-btn";
        deleteButton.textContent = "Delete";

        deleteButton.addEventListener("click", function() {
            deleteVehicle(vehicle.vehicleId);
        });

        card.appendChild(details);
        card.appendChild(deleteButton);

        vehicleList.appendChild(card);

    });

}


// Add a vehicle
vehicleForm.addEventListener("submit", function(event) {

    event.preventDefault();

    const vehicleNumber = document.getElementById("vehicleNumber")
        .value.trim().toUpperCase();

    const vehicleType = document.getElementById("vehicleType").value;

    const vehicleModel = document.getElementById("vehicleModel")
        .value.trim();

    const vehicleColor = document.getElementById("vehicleColor")
        .value.trim();

    const vehiclePattern = /^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$/;

    if (!vehiclePattern.test(vehicleNumber)) {

        vehicleMessage.textContent =
            "Enter a valid vehicle number. Example: AP07AB1234";

        return;

    }

    if (vehicleType === "") {

        vehicleMessage.textContent = "Please select vehicle type.";

        return;

    }

    if (vehicleModel.length < 2 || vehicleColor.length < 2) {

        vehicleMessage.textContent =
            "Please enter valid vehicle model and color.";

        return;

    }

    const vehicles = getVehicles();

    const duplicate = vehicles.some(function(vehicle) {

        return vehicle.vehicleNumber === vehicleNumber;

    });

    if (duplicate) {

        vehicleMessage.textContent =
            "This vehicle is already registered.";

        return;

    }

    const vehicleData = {

        vehicleId: "VEH" + Date.now(),

        vehicleNumber: vehicleNumber,

        vehicleType: vehicleType,

        vehicleModel: vehicleModel,

        vehicleColor: vehicleColor

    };

    vehicles.push(vehicleData);

    saveVehicles(vehicles);

    vehicleForm.reset();

    vehicleMessage.style.color = "#16A34A";
    vehicleMessage.textContent = "Vehicle registered successfully!";

    displayVehicles();

});


// Delete a vehicle
function deleteVehicle(vehicleId) {

    const confirmDelete = confirm(
        "Are you sure you want to delete this vehicle?"
    );

    if (!confirmDelete) {
        return;
    }

    const vehicles = getVehicles();

    const updatedVehicles = vehicles.filter(function(vehicle) {

        return vehicle.vehicleId !== vehicleId;

    });

    saveVehicles(updatedVehicles);

    displayVehicles();

}


// Display saved vehicles on page load
displayVehicles();
