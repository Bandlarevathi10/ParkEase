
document.addEventListener("DOMContentLoaded", function () {

    const cityInput = document.getElementById("city");
    const locationInput = document.getElementById("location");
    const vehicleSelect = document.getElementById("vehicleType");
    const searchBtn = document.getElementById("searchBtn");

    const parkingResults = document.getElementById("parkingResults");
    const resultCount = document.getElementById("resultCount");
    const noResults = document.getElementById("noResults");

    let allParkings = [];

    // Load parking locations from owner_db
    loadParkings();

    searchBtn.addEventListener("click", searchParking);

    [cityInput, locationInput].forEach(function (input) {

        input.addEventListener("keydown", function (event) {

            if (event.key === "Enter") {
                searchParking();
            }

        });

    });

    vehicleSelect.addEventListener("change", searchParking);

    function loadParkings() {

        parkingResults.innerHTML = "<p>Loading parking locations...</p>";

        const url = "http://localhost:8081/vehicle_backend/Gateway";

        const formData = new URLSearchParams();

        formData.append("action", "Search");

        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            credentials: "include",
            body: formData.toString()
        })

        .then(function (response) {

            if (!response.ok) {
                throw new Error("Server error: " + response.status);
            }

            return response.json();

        })

        .then(function (data) {

            console.log("Parking response:", data);

            if (!data.success) {
                throw new Error(data.message || "Unable to load parking");
            }

            allParkings = data.parkings || [];

            searchParking();

        })

        .catch(function (error) {

            console.error("Parking loading error:", error);

            parkingResults.innerHTML =
                "<p>Unable to load parking locations. Please check the backend.</p>";

            resultCount.textContent = "0 Locations";

        });

    }

    function searchParking() {

        const city = cityInput.value.trim().toLowerCase();
        const location = locationInput.value.trim().toLowerCase();
        const vehicle = vehicleSelect.value.toUpperCase();

        const filteredParkings = allParkings.filter(function (parking) {

            const parkingCity = (parking.city || "").toLowerCase();

            const parkingLocation =
                (parking.location || "").toLowerCase();

            const parkingName =
                (parking.parkingName || "").toLowerCase();

            const cityMatch =
                city === "" || parkingCity.includes(city);

            const locationMatch =
                location === "" ||
                parkingLocation.includes(location) ||
                parkingName.includes(location);

            const vehicleMatch =
                vehicle === "" ||
                (vehicle === "CAR" && Number(parking.carSlots) > 0) ||
                (vehicle === "BIKE" && Number(parking.bikeSlots) > 0);

            return cityMatch && locationMatch && vehicleMatch;

        });

        displayParkings(filteredParkings);

    }

    function displayParkings(parkings) {

        // Remove old cards but preserve the no-results message
        parkingResults.innerHTML = "";

        parkings.forEach(function (parking) {

            const card = document.createElement("div");

            card.className = "parking-card";

            const vehicleText = [];

            if (Number(parking.carSlots) > 0) {
                vehicleText.push("Car (" + parking.carSlots + ")");
            }

            if (Number(parking.bikeSlots) > 0) {
                vehicleText.push("Bike (" + parking.bikeSlots + ")");
            }

            const available = Number(parking.availableSlots) || 0;

            card.innerHTML = `
                <div class="parking-image image-one">
                    <span class="available-tag">
                        ${available > 0 ? "Available" : "Full"}
                    </span>
                </div>

                <div class="parking-details">

                    <h3>${escapeHTML(parking.parkingName)}</h3>

                    <p>
                        📍 ${escapeHTML(parking.location)},
                        ${escapeHTML(parking.city)}
                    </p>

                    <p>
                        ${escapeHTML(parking.address)}
                    </p>

                    <div class="parking-info">
                        <span>🚗 ${vehicleText.join(" & ") || "No vehicle slots"}</span>
                        <span>${available} Slots Available</span>
                    </div>

                    <div class="price-row">

                        <div>
                            <small>Starting from</small>
                            <h3>
                                ₹${Number(parking.pricePerHour).toFixed(2)}
                                <small>/ hour</small>
                            </h3>
                        </div>

                        <button type="button" class="book-btn">
                            View Slots
                        </button>

                    </div>

                </div>
            `;

            // Store actual database parking ID
            card.querySelector(".book-btn").addEventListener("click", function () {

                bookParking(
                    parking.parkingId,
                    parking.parkingName,
                    parking.location + ", " + parking.city
                );

            });

            parkingResults.appendChild(card);

        });

        resultCount.textContent = parkings.length + " Locations";

        if (parkings.length === 0) {

            const message = document.createElement("p");

            message.className = "no-results";

            message.textContent =
                "No parking locations found. Try another search.";

            parkingResults.appendChild(message);
        }

    }

    function escapeHTML(value) {

        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");

    }

});

function bookParking(parkingId, parkingName, parkingLocation) {

    sessionStorage.setItem("selectedParkingId", parkingId);

    sessionStorage.setItem("selectedParkingName", parkingName || "");

    sessionStorage.setItem("selectedParkingLocation", parkingLocation || "");

    // Clear previous slot selection
    sessionStorage.removeItem("selectedSlot");
    sessionStorage.removeItem("selectedSlotId");
    sessionStorage.removeItem("selectedSlotNumber");

    window.location.href = "parkingslots.html";

}
