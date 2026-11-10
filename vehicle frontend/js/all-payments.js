
const API_URL =
    "http://localhost:8081/vehicle_backend/ParkingControllerr?paymentAction=view";

let allPayments = [];

document.addEventListener("DOMContentLoaded", function () {

    loadTransactions();

    document.getElementById("searchInput")
        .addEventListener("input", filterTransactions);

    document.getElementById("statusFilter")
        .addEventListener("change", filterTransactions);

});

async function loadTransactions() {

    const message = document.getElementById("message");
    const table = document.getElementById("transactionTable");

    try {

        const response = await fetch(API_URL, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || "Unable to load transactions.");
        }

        allPayments = data.payments || [];

        updateSummary(allPayments);
        displayTransactions(allPayments);

        message.textContent =
            allPayments.length + " transactions found.";

    } catch (error) {

        console.error("Transaction loading error:", error);

        table.innerHTML =
            '<tr><td colspan="13">Unable to load transactions.</td></tr>';

        message.textContent =
            "Error loading transactions. Please check your login session and backend.";

    }
}

function updateSummary(payments) {

    const successful = payments.filter(
        p => normalizeStatus(p.paymentStatus) === "SUCCESS"
    );

    const pending = payments.filter(
        p => normalizeStatus(p.paymentStatus) === "PENDING"
    );

    const failed = payments.filter(
        p => normalizeStatus(p.paymentStatus) === "FAILED"
    );

    const revenue = successful.reduce(
        (total, p) => total + Number(p.amount || 0),
        0
    );

    document.getElementById("totalTransactions").textContent =
        payments.length;

    document.getElementById("totalRevenue").textContent =
        "₹" + revenue.toFixed(2);

    document.getElementById("successfulTransactions").textContent =
        successful.length;

    document.getElementById("pendingTransactions").textContent =
        pending.length;

    document.getElementById("failedTransactions").textContent =
        failed.length;
}

function filterTransactions() {

    const search = document.getElementById("searchInput")
        .value.toLowerCase().trim();

    const status = document.getElementById("statusFilter")
        .value;

    const filtered = allPayments.filter(payment => {

        const searchText = [
            payment.customerName,
            payment.vehicleNumber,
            payment.transactionId,
            payment.parkingName,
            payment.bookingId
        ].join(" ").toLowerCase();

        const matchesSearch = searchText.includes(search);

        const matchesStatus =
            status === "ALL" ||
            normalizeStatus(payment.paymentStatus) === status;

        return matchesSearch && matchesStatus;
    });

    displayTransactions(filtered);

    document.getElementById("message").textContent =
        filtered.length + " transactions found.";
}

function displayTransactions(payments) {

    const table = document.getElementById("transactionTable");

    if (payments.length === 0) {

        table.innerHTML =
            '<tr><td colspan="13">No transactions found.</td></tr>';

        return;
    }

    table.innerHTML = payments.map(p => {

        const status = normalizeStatus(p.paymentStatus);

        return `
            <tr>
                <td>${escapeHTML(p.paymentId)}</td>
                <td>${escapeHTML(p.transactionId)}</td>
                <td>${escapeHTML(p.bookingId)}</td>
                <td>${escapeHTML(p.customerName)}</td>
                <td>
                    ${escapeHTML(p.vehicleNumber)}
                    (${escapeHTML(p.vehicleType)})
                </td>
                <td>${escapeHTML(p.parkingName)}</td>
                <td>${escapeHTML(p.slotNumber)}</td>
                <td>₹${Number(p.amount || 0).toFixed(2)}</td>
                <td>${escapeHTML(p.paymentMethod)}</td>
                <td>
                    <span class="status ${status}">
                        ${escapeHTML(status)}
                    </span>
                </td>
                <td>${formatDate(p.paymentDate)}</td>
                <td>${formatDate(p.startTime)}</td>
                <td>${formatDate(p.endTime)}</td>
            </tr>
        `;

    }).join("");
}

function normalizeStatus(value) {
    return String(value || "UNKNOWN").toUpperCase();
}

function escapeHTML(value) {

    return String(value ?? "").replace(/[&<>"']/g, function (character) {

        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        };

        return entities[character];
    });
}

function formatDate(value) {

    if (!value) {
        return "-";
    }

    const date = new Date(value.replace(" ", "T"));

    if (isNaN(date.getTime())) {
        return escapeHTML(value);
    }

    return date.toLocaleString("en-IN");
}
