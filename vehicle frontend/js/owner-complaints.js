
const API =
    "http://localhost:8081/vehicle_backend/OwnerComplaintController";

document.addEventListener("DOMContentLoaded", loadComplaints);

async function loadComplaints() {

    const table = document.getElementById("complaintTable");
    const message = document.getElementById("message");

    table.innerHTML =
        '<tr><td colspan="9">Loading complaints...</td></tr>';

    message.textContent = "";

    try {

        const response = await fetch(API, {
            method: "GET",
            credentials: "include"
        });

        const data = await response.json();

        if (!response.ok || !data.success) {
            throw new Error(data.message || "Unable to load complaints.");
        }

        displayComplaints(data.complaints);

    } catch (error) {

        console.error("Complaint loading error:", error);

        table.innerHTML =
            '<tr><td colspan="9" class="empty-message">' +
            escapeHTML(error.message) +
            '</td></tr>';

    }
}

function displayComplaints(complaints) {

    const table = document.getElementById("complaintTable");

    let open = 0;
    let progress = 0;
    let resolved = 0;

    document.getElementById("totalComplaints").textContent =
        complaints.length;

    if (complaints.length === 0) {

        table.innerHTML =
            '<tr><td colspan="9" class="empty-message">' +
            'No complaints submitted yet.' +
            '</td></tr>';

        updateCounts(0, 0, 0);
        return;
    }

    let html = "";

    complaints.forEach(c => {

        if (c.status === "OPEN") open++;
        else if (c.status === "IN PROGRESS") progress++;
        else if (c.status === "RESOLVED") resolved++;

        const booking = c.bookingId === null
            ? "Not provided"
            : c.bookingId;

        const date = c.date
            ? new Date(c.date.replace(" ", "T")).toLocaleString()
            : "-";

        html += `
            <tr>

                <td>CMP${c.complaintId}</td>

                <td>${escapeHTML(c.customer || "Unknown")}</td>

                <td>${escapeHTML(c.parkingName || "Not linked")}</td>

                <td>${booking}</td>

                <td>${escapeHTML(c.subject)}</td>

                <td class="description-cell">
                    ${escapeHTML(c.description)}
                </td>

                <td>${escapeHTML(date)}</td>

                <td>
                    ${escapeHTML(c.status)}
                </td>

                <td>

                    <select
                        class="status-select"
                        id="status-${c.complaintId}">

                        <option value="OPEN"
                            ${c.status === "OPEN" ? "selected" : ""}>
                            OPEN
                        </option>

                        <option value="IN PROGRESS"
                            ${c.status === "IN PROGRESS" ? "selected" : ""}>
                            IN PROGRESS
                        </option>

                        <option value="RESOLVED"
                            ${c.status === "RESOLVED" ? "selected" : ""}>
                            RESOLVED
                        </option>

                    </select>

                    <button
                        class="update-btn"
                        onclick="updateComplaint(${c.complaintId})">

                        Update

                    </button>

                </td>

            </tr>
        `;
    });

    table.innerHTML = html;

    updateCounts(open, progress, resolved);
}

function updateCounts(open, progress, resolved) {

    document.getElementById("openComplaints").textContent = open;

    document.getElementById("progressComplaints").textContent = progress;

    document.getElementById("resolvedComplaints").textContent = resolved;
}

async function updateComplaint(complaintId) {

    const statusSelect =
        document.getElementById("status-" + complaintId);

    const status = statusSelect.value;

    const message = document.getElementById("message");

    const data = new URLSearchParams();

    data.append("action", "updateStatus");
    data.append("complaintId", complaintId);
    data.append("status", status);

    try {

        const response = await fetch(API, {

            method: "POST",

            credentials: "include",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: data.toString()

        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || "Status update failed.");
        }

        message.textContent = "Complaint status updated successfully.";

        await loadComplaints();

    } catch (error) {

        console.error("Status update error:", error);

        message.textContent = error.message;
    }
}

function escapeHTML(value) {

    return String(value ?? "").replace(/[&<>"']/g, function(char) {

        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        }[char];

    });
}
