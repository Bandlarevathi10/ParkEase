
document.addEventListener("DOMContentLoaded", function () {

    loadDashboard();

});


async function loadDashboard() {

    try {

        const response = await fetch(
            "http://localhost:8081/vehicle_backend/DashboardControllerr",
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (response.status === 401) {
            alert("Your session has expired. Please login again.");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || "Unable to load dashboard");
        }

        // Display user's name
        document.getElementById("userName").textContent =
            data.userName || "User";

        // Display statistics
        document.getElementById("totalBookings").textContent =
            data.totalBookings;

        document.getElementById("availableSlots").textContent =
            data.availableSlots;

        document.getElementById("activeBookings").textContent =
            data.activeBookings;

        document.getElementById("totalVehicles").textContent =
            data.totalVehicles;

        // Display first letter in avatar
        const avatar = document.querySelector(".user-avatar");

        if (avatar) {
            avatar.textContent =
                (data.userName || "U").charAt(0).toUpperCase();
        }

        // Display recent bookings
        displayBookings(data.bookings);

        console.log("Dashboard connected to MySQL successfully");

    } catch (error) {

        console.error("Dashboard error:", error);

        document.getElementById("userName").textContent =
            "Unable to load";

        document.getElementById("bookingTable").innerHTML =
            `<tr><td colspan="6">Unable to load bookings. Check the server console.</td></tr>`;

    }

}


// Display booking records
function displayBookings(bookings) {

    const table = document.getElementById("bookingTable");

    table.innerHTML = "";

    if (!bookings || bookings.length === 0) {

        table.innerHTML =
            `<tr><td colspan="6">No bookings found.</td></tr>`;

        return;
    }

    bookings.forEach(function (booking) {

        const row = document.createElement("tr");

        const values = [
            "BK" + booking.bookingId,
            booking.location || "N/A",
            booking.vehicle || "N/A",
            formatDate(booking.date),
            "₹" + booking.amount,
            booking.status || "Pending"
        ];

        values.forEach(function (value, index) {

            const cell = document.createElement("td");

            if (index === 5) {

                const status = document.createElement("span");
                status.classList.add("status");

                const statusText = String(value);
                const normalized = statusText.toLowerCase();

                if (["confirmed", "active"].includes(normalized)) {
                    status.classList.add("active-status");
                } else if (normalized === "completed") {
                    status.classList.add("completed-status");
                } else {
                    status.classList.add("pending-status");
                }

                status.textContent = statusText;
                cell.appendChild(status);

            } else {

                cell.textContent = value;

            }

            row.appendChild(cell);

        });

        table.appendChild(row);

    });

}


// Format booking date
function formatDate(dateValue) {

    if (!dateValue) {
        return "N/A";
    }

    const date = new Date(dateValue.replace(" ", "T"));

    if (isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });

}
document.addEventListener("DOMContentLoaded", function () {
    loadLoggedUserName();
});

function loadLoggedUserName() {

    fetch("http://localhost:8081/vehicle_backend/UserProfileController", {
        method: "GET",
        credentials: "include"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Unable to get user details");
        }
        return response.json();
    })
    .then(data => {

        if (data.success) {
            document.getElementById("loggedUserName").textContent = data.name;
        }

    })
    .catch(error => {
        console.error("Error loading user name:", error);
    });
}

document.addEventListener("DOMContentLoaded", function () {
    loadDashboard();
});

async function loadDashboard() {

    try {

        const response = await fetch(
            "http://localhost:8081/vehicle_backend/DashboardControllerr",
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (response.status === 401) {
            alert("Your session has expired. Please login again.");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || "Unable to load dashboard");
        }

        // Display logged-in user's name
        const name = data.userName || "User";

        document.getElementById("loggedUserName").textContent = name;

        // Display first letter in avatar
        document.getElementById("userAvatar").textContent =
            name.charAt(0).toUpperCase();

        // Display statistics
        document.getElementById("totalBookings").textContent =
            data.totalBookings;

        document.getElementById("availableSlots").textContent =
            data.availableSlots;

        document.getElementById("activeBookings").textContent =
            data.activeBookings;

        document.getElementById("totalVehicles").textContent =
            data.totalVehicles;

        // Display recent bookings
        displayBookings(data.bookings);

        console.log("Dashboard loaded for:", name);

    } catch (error) {

        console.error("Dashboard error:", error);

        document.getElementById("loggedUserName").textContent =
            "Unable to load";

        document.getElementById("bookingTable").innerHTML =
            `<tr><td colspan="6">Unable to load bookings. Check the server console.</td></tr>`;
    }
}


// Display booking records
function displayBookings(bookings) {

    const table = document.getElementById("bookingTable");

    table.innerHTML = "";

    if (!bookings || bookings.length === 0) {

        table.innerHTML =
            `<tr><td colspan="6">No bookings found.</td></tr>`;

        return;
    }

    bookings.forEach(function (booking) {

        const row = document.createElement("tr");

        const values = [
            "BK" + booking.bookingId,
            booking.location || "N/A",
            booking.vehicle || "N/A",
            formatDate(booking.date),
            "₹" + booking.amount,
            booking.status || "Pending"
        ];

        values.forEach(function (value, index) {

            const cell = document.createElement("td");

            if (index === 5) {

                const status = document.createElement("span");
                status.classList.add("status");

                const normalized = String(value).toLowerCase();

                if (["confirmed", "active"].includes(normalized)) {
                    status.classList.add("active-status");
                } else if (normalized === "completed") {
                    status.classList.add("completed-status");
                } else {
                    status.classList.add("pending-status");
                }

                status.textContent = value;
                cell.appendChild(status);

            } else {
                cell.textContent = value;
            }

            row.appendChild(cell);
        });

        table.appendChild(row);
    });
}


// Format booking date
function formatDate(dateValue) {

    if (!dateValue) {
        return "N/A";
    }

    const date = new Date(dateValue.replace(" ", "T"));

    if (isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}
