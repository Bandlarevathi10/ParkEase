

const bookingTableBody = document.getElementById("bookingTableBody");

const searchBooking = document.getElementById("searchBooking");
const statusFilter = document.getElementById("statusFilter");
const parkingFilter = document.getElementById("parkingFilter");

// Read demo bookings
let bookings = JSON.parse(
    sessionStorage.getItem("bookingList")
) || [];

const latestBooking = JSON.parse(
    sessionStorage.getItem("bookingData")
);

if (latestBooking) {

    const exists = bookings.some(function(b) {
        return b.bookingId === latestBooking.bookingId;
    });

    if (!exists) {
        bookings.push(latestBooking);
        sessionStorage.setItem("bookingList", JSON.stringify(bookings));
    }
}

let parkingList = JSON.parse(
    sessionStorage.getItem("ownerParkingList")
) || [];

// Load parking filter
function loadParkingFilter() {

    parkingList.forEach(function(parking) {

        const option = document.createElement("option");

        option.value = parking.id;
        option.textContent = parking.parkingName;

        parkingFilter.appendChild(option);
    });
}

// Statistics
function updateStatistics() {

    document.getElementById("totalBookings").textContent =
        bookings.length;

    document.getElementById("pendingBookings").textContent =
        bookings.filter(b => b.bookingStatus === "PENDING").length;

    document.getElementById("confirmedBookings").textContent =
        bookings.filter(b => b.bookingStatus === "CONFIRMED").length;

    document.getElementById("cancelledBookings").textContent =
        bookings.filter(b => b.bookingStatus === "CANCELLED").length;
}

// Display booking table
function displayBookings() {

    const search = searchBooking.value.toLowerCase();
    const status = statusFilter.value;
    const parkingId = parkingFilter.value;

    const filtered = bookings.filter(function(b) {

        const bookingId = String(b.bookingId || "").toLowerCase();
        const vehicle = String(b.vehicleNumber || "").toLowerCase();

        const matchesSearch =
            bookingId.includes(search) || vehicle.includes(search);

        const matchesStatus =
            status === "ALL" || b.bookingStatus === status;

        const matchesParking =
            parkingId === "ALL" ||
            String(b.parkingId) === parkingId;

        return matchesSearch && matchesStatus && matchesParking;
    });

    bookingTableBody.innerHTML = "";

    document.getElementById("bookingCount").textContent =
        filtered.length + " Bookings";

    if (filtered.length === 0) {

        bookingTableBody.innerHTML =
            '<tr><td colspan="10" class="empty">No bookings found.</td></tr>';

        return;
    }

    filtered.forEach(function(b) {

        const row = document.createElement("tr");

        const parking = parkingList.find(
            p => Number(p.id) === Number(b.parkingId)
        );

        const values = [
            b.bookingId || "DEMO",
            b.customerName || "Customer",
            b.vehicleNumber || "Not available",
            parking ? parking.parkingName : (b.parkingName || "Unknown"),
            b.slotNumber || "N/A",
            b.parkingDate || "N/A",
            (b.startTime || "") + " - " + (b.endTime || ""),
            "₹" + (Number(b.totalAmount) || 0).toFixed(2)
        ];

        values.forEach(function(value) {

            const td = document.createElement("td");
            td.textContent = value;

            row.appendChild(td);
        });

        // Status
        const statusCell = document.createElement("td");

        const badge = document.createElement("span");

        badge.className = "status " + b.bookingStatus;
        badge.textContent = b.bookingStatus || "PENDING";

        statusCell.appendChild(badge);
        row.appendChild(statusCell);

        // Action
        const actionCell = document.createElement("td");

        const select = document.createElement("select");

        select.className = "action-select";

        const options = [
            "PENDING",
            "CONFIRMED",
            "CANCELLED",
            "COMPLETED"
        ];

        options.forEach(function(optionValue) {

            const option = document.createElement("option");

            option.value = optionValue;
            option.textContent = optionValue;

            if (optionValue === b.bookingStatus) {
                option.selected = true;
            }

            select.appendChild(option);
        });

        select.disabled = ["CANCELLED", "COMPLETED"].includes(
            b.bookingStatus
        );

        select.addEventListener("change", function() {

            updateBookingStatus(b.bookingId, select.value);
        });

        actionCell.appendChild(select);
        row.appendChild(actionCell);

        bookingTableBody.appendChild(row);
    });
}

// Update booking status
function updateBookingStatus(id, newStatus) {

    const confirmUpdate = confirm(
        "Do you want to change this booking status to " + newStatus + "?"
    );

    if (!confirmUpdate) {
        displayBookings();
        return;
    }

    const index = bookings.findIndex(
        b => b.bookingId === id
    );

    if (index === -1) return;

    bookings[index].bookingStatus = newStatus;

    sessionStorage.setItem(
        "bookingList",
        JSON.stringify(bookings)
    );

    // Update latest booking if the same booking is stored there
    const latest = JSON.parse(
        sessionStorage.getItem("bookingData")
    );

    if (latest && latest.bookingId === id) {

        latest.bookingStatus = newStatus;

        sessionStorage.setItem(
            "bookingData",
            JSON.stringify(latest)
        );
    }

    updateStatistics();
    displayBookings();
}

// Search and filter
searchBooking.addEventListener("input", displayBookings);
statusFilter.addEventListener("change", displayBookings);
parkingFilter.addEventListener("change", displayBookings);

// Initialize
loadParkingFilter();
updateStatistics();
displayBookings();
