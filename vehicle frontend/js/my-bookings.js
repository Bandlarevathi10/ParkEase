
const bookingTableBody = document.getElementById("bookingTableBody");


// Get all demo bookings
function getBookings() {

    let bookings = JSON.parse(
        sessionStorage.getItem("bookingList")
    ) || [];

    // Include the booking created by the existing booking.js page
    const latestBooking = JSON.parse(
        sessionStorage.getItem("bookingData")
    );

    if (latestBooking) {

        const alreadyExists = bookings.some(function(booking) {
            return booking.bookingId === latestBooking.bookingId;
        });

        if (!alreadyExists) {

            latestBooking.bookingId =
                latestBooking.bookingId || "DEMO" + Date.now();

            bookings.push(latestBooking);

            sessionStorage.setItem(
                "bookingData",
                JSON.stringify(latestBooking)
            );

            sessionStorage.setItem(
                "bookingList",
                JSON.stringify(bookings)
            );

        }

    }

    return bookings;

}


// Display booking history
function displayBookings() {

    const bookings = getBookings();

    bookingTableBody.innerHTML = "";

    let pending = 0;
    let cancelled = 0;

    bookings.forEach(function(booking) {

        const status = booking.bookingStatus || "PENDING";

        if (status === "CANCELLED") {
            cancelled++;
        } else if (status === "PENDING") {
            pending++;
        }

        const row = document.createElement("tr");

        const bookingId = booking.bookingId || "--";
        const parkingName = booking.parkingName || "--";
        const slotNumber = booking.slotNumber || "--";
        const vehicleNumber = booking.vehicleNumber || "--";
        const parkingDate = booking.parkingDate || "--";

        const time =
            (booking.startTime || "--") +
            " - " +
            (booking.endTime || "--");

        const amount =
            "₹" + Number(booking.totalAmount || 0).toFixed(2);

        const statusClass =
            status === "CANCELLED"
                ? "status-cancelled"
                : status === "CONFIRMED"
                    ? "status-confirmed"
                    : "status-pending";

        const values = [
            bookingId,
            parkingName,
            slotNumber,
            vehicleNumber,
            parkingDate,
            time,
            amount
        ];

        values.forEach(function(value) {

            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);

        });

        // Status column
        const statusCell = document.createElement("td");

        const statusLabel = document.createElement("span");

        statusLabel.className = "status " + statusClass;
        statusLabel.textContent = status;

        statusCell.appendChild(statusLabel);
        row.appendChild(statusCell);

        // Action column
        const actionCell = document.createElement("td");

        const cancelButton = document.createElement("button");

        cancelButton.className = "cancel-btn";
        cancelButton.textContent = "Cancel";

        // Only pending bookings can be cancelled in this demo
        if (status !== "PENDING") {

            cancelButton.disabled = true;
            cancelButton.textContent = "Not Available";

        } else {

            cancelButton.addEventListener("click", function() {

                cancelBooking(booking.bookingId);

            });

        }

        actionCell.appendChild(cancelButton);
        row.appendChild(actionCell);

        bookingTableBody.appendChild(row);

    });

    if (bookings.length === 0) {

        bookingTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="empty-message">
                    No bookings available. Create your first booking!
                </td>
            </tr>
        `;

    }

    document.getElementById("totalBookings").textContent =
        bookings.length;

    document.getElementById("pendingBookings").textContent =
        pending;

    document.getElementById("cancelledBookings").textContent =
        cancelled;

}


// Cancel booking
function cancelBooking(bookingId) {

    const confirmCancel = confirm(
        "Are you sure you want to cancel this booking?"
    );

    if (!confirmCancel) {
        return;
    }

    let bookings = JSON.parse(
        sessionStorage.getItem("bookingList")
    ) || [];

    const bookingIndex = bookings.findIndex(function(booking) {

        return booking.bookingId === bookingId;

    });

    if (bookingIndex === -1) {

        document.getElementById("cancelMessage").textContent =
            "Booking not found.";

        return;

    }

    if (bookings[bookingIndex].bookingStatus !== "PENDING") {

        document.getElementById("cancelMessage").textContent =
            "Only pending bookings can be cancelled.";

        return;

    }

    // Change booking status
    bookings[bookingIndex].bookingStatus = "CANCELLED";

    // Save updated booking list
    sessionStorage.setItem(
        "bookingList",
        JSON.stringify(bookings)
    );

    // Update the original booking data too
    const latestBooking = JSON.parse(
        sessionStorage.getItem("bookingData")
    );

    if (latestBooking &&
        latestBooking.bookingId === bookingId) {

        latestBooking.bookingStatus = "CANCELLED";

        sessionStorage.setItem(
            "bookingData",
            JSON.stringify(latestBooking)
        );

    }

    document.getElementById("cancelMessage").textContent =
        "Booking " + bookingId + " cancelled successfully in this demo.";

    displayBookings();

}


// Load bookings
displayBookings();
