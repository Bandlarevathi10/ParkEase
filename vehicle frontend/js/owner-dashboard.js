
document.addEventListener("DOMContentLoaded", function () {

    function updateText(id, value) {
        const element = document.getElementById(id);
        
        if (element) {
            element.textContent = value ?? "0";
        } else {
            console.warn("Missing dashboard HTML element:", id);
        }
    }

    fetch("http://localhost:8081/vehicle_backend/OwnerDashboardController", {
        method: "GET",
        credentials: "include"
    })
    .then(function (response) {

        if (!response.ok) {
            throw new Error(
                response.status === 401
                    ? "Please login again as an owner."
                    : "Unable to load dashboard. HTTP " + response.status
            );
        }

        return response.text();
    })
    .then(function (html) {

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, "text/html");

        const data = doc.getElementById("ownerDashboardData");

        if (!data) {
            throw new Error("Dashboard data not found in server response.");
        }

        // Owner name
        updateText("ownerName", data.dataset.ownerName);

        // Main statistics
        updateText("totalParking", data.dataset.totalParking);
        updateText("availableSlots", data.dataset.availableSlots);
        updateText("bookedSlots", data.dataset.bookedSlots);

        updateText(
            "totalEarnings",
            "₹" + Number(data.dataset.totalEarnings || 0).toFixed(2)
        );

        // Parking slot overview
        updateText("overviewAvailable", data.dataset.availableSlots);
        updateText("overviewBooked", data.dataset.bookedSlots);
        updateText("overviewMaintenance", data.dataset.maintenanceSlots);

        // Recent bookings
        const bookingRows = doc.getElementById("bookingRows");
        const bookingTable = document.getElementById("bookingTable");

        if (bookingRows && bookingTable) {
            bookingTable.innerHTML = bookingRows.innerHTML;
        } else {
            console.warn("Recent booking table or rows not found.");
        }

    })
    .catch(function (error) {

        console.error("Owner Dashboard Error:", error);
        alert(error.message);

    });

});
