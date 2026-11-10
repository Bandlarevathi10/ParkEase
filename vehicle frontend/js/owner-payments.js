const API_URL =
    "http://localhost:8081/vehicle_backend/OwnerDashboardController?action=payments";

let payments = [];

function updateStatistics() {
    let totalRevenue = 0;
    let success = 0;
    let pending = 0;
    let failed = 0;

    payments.forEach(payment => {
        const status = (payment.status || "").toUpperCase();

        if (status === "SUCCESS" || status === "PAID" || status === "COMPLETED") {
            totalRevenue += Number(payment.amount) || 0;
            success++;
        } else if (status === "PENDING") {
            pending++;
        } else if (status === "FAILED") {
            failed++;
        }
    });

    document.getElementById("totalRevenue").textContent =
        "₹" + totalRevenue.toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });

    document.getElementById("successPayments").textContent = success;
    document.getElementById("pendingPayments").textContent = pending;
    document.getElementById("failedPayments").textContent = failed;
}

function displayPayments(data) {
    const tableBody = document.getElementById("paymentTableBody");
    const noData = document.getElementById("noData");

    tableBody.innerHTML = "";

    if (data.length === 0) {
        noData.style.display = "block";
        return;
    }

    noData.style.display = "none";

    data.forEach(payment => {
        const row = document.createElement("tr");

        const values = [
            "#" + payment.paymentId,
            "#" + payment.bookingId,
            payment.customer,
            payment.parking,
            "₹" + Number(payment.amount).toFixed(2),
            payment.method || "-",
            payment.transactionId || "-",
            payment.date ? payment.date.substring(0, 10) : "-",
            payment.status || "UNKNOWN"
        ];

        values.forEach((value, index) => {
            const cell = document.createElement("td");

            if (index === 4) {
                cell.className = "amount";
            }

            if (index === 8) {
                const span = document.createElement("span");
                const status = String(value).toUpperCase();

                span.className = "status " + status.toLowerCase();
                span.textContent = status;
                cell.appendChild(span);
            } else {
                cell.textContent = value;
            }

            row.appendChild(cell);
        });

        tableBody.appendChild(row);
    });
}

function filterPayments() {
    const searchValue =
        document.getElementById("searchInput").value.toLowerCase();

    const statusValue =
        document.getElementById("statusFilter").value;

    const methodValue =
        document.getElementById("methodFilter").value;

    const filteredPayments = payments.filter(payment => {
        const matchesSearch =
            String(payment.bookingId).includes(searchValue) ||
            String(payment.paymentId).includes(searchValue) ||
            String(payment.transactionId || "").toLowerCase().includes(searchValue) ||
            String(payment.customer || "").toLowerCase().includes(searchValue);

        const matchesStatus =
            statusValue === "ALL" ||
            String(payment.status).toUpperCase() === statusValue;

        const matchesMethod =
            methodValue === "ALL" ||
            String(payment.method).toUpperCase() === methodValue;

        return matchesSearch && matchesStatus && matchesMethod;
    });

    displayPayments(filteredPayments);
}

function exportPayments() {
    if (payments.length === 0) {
        alert("No payment records available!");
        return;
    }

    const csv = [
        [
            "Payment ID", "Booking ID", "Customer", "Parking",
            "Amount", "Method", "Transaction ID", "Date", "Status"
        ]
    ];

    payments.forEach(payment => {
        csv.push([
            payment.paymentId,
            payment.bookingId,
            payment.customer,
            payment.parking,
            payment.amount,
            payment.method,
            payment.transactionId,
            payment.date,
            payment.status
        ]);
    });

    const csvContent = csv
        .map(row => row.map(value =>
            '"' + String(value ?? "").replace(/"/g, '""') + '"'
        ).join(","))
        .join("\n");

    const blob = new Blob(["\uFEFF" + csvContent], {
        type: "text/csv;charset=utf-8;"
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "ParkEase-Payment-Report.csv";

    document.body.appendChild(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(url);
}

function loadPayments() {
    fetch(API_URL, {
        method: "GET",
        credentials: "include"
    })
    .then(response => {
        if (response.status === 401) {
            throw new Error("Please login as an owner.");
        }

        if (!response.ok) {
            throw new Error("Unable to load payments. HTTP " + response.status);
        }

        return response.json();
    })
    .then(data => {
        if (!Array.isArray(data)) {
            throw new Error(data.error || "Invalid payment data received.");
        }

        payments = data;
        updateStatistics();
        filterPayments();
    })
    .catch(error => {
        console.error("Payment loading error:", error);
        document.getElementById("noData").style.display = "block";
        document.getElementById("noData").querySelector("p").textContent =
            error.message;
    });
}

// Event listeners
document.getElementById("searchInput")
    .addEventListener("input", filterPayments);

document.getElementById("statusFilter")
    .addEventListener("change", filterPayments);

document.getElementById("methodFilter")
    .addEventListener("change", filterPayments);

document.getElementById("exportBtn")
    .addEventListener("click", exportPayments);

// Logout
document.getElementById("logoutBtn")
    .addEventListener("click", function(event) {
        event.preventDefault();

        if (confirm("Are you sure you want to logout?")) {
            window.location.href = "login.html";
        }
    });

// Load actual MySQL payment records
loadPayments();