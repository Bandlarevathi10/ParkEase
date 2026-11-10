
const FEEDBACK_URL =
    "http://localhost:8081/vehicle_backend/OwnerFeedbackController";

const feedbackTable = document.getElementById("feedbackTable");
const message = document.getElementById("message");

async function loadFeedback() {

    message.textContent = "Loading feedback...";
    message.style.display = "block";

    feedbackTable.innerHTML = `
        <tr>
            <td colspan="6" class="empty-row">Loading feedback...</td>
        </tr>
    `;

    try {

        const response = await fetch(FEEDBACK_URL, {
            method: "GET",
            credentials: "include"
        });

        if (response.status === 401) {
            throw new Error("Please login using your owner account.");
        }

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || "Unable to load feedback.");
        }

        const feedbackList = data.feedback || [];

        document.getElementById("totalFeedback").textContent =
            feedbackList.length;

        let totalRating = 0;
        let fiveStarCount = 0;

        feedbackList.forEach(item => {
            totalRating += Number(item.rating);

            if (Number(item.rating) === 5) {
                fiveStarCount++;
            }
        });

        const average = feedbackList.length > 0
            ? (totalRating / feedbackList.length).toFixed(1)
            : "0.0";

        document.getElementById("averageRating").textContent = average;
        document.getElementById("fiveStar").textContent = fiveStarCount;

        if (feedbackList.length === 0) {

            feedbackTable.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-row">
                        No customer feedback available yet.
                    </td>
                </tr>
            `;

            message.textContent = "No feedback records found.";
            return;
        }

        feedbackTable.innerHTML = "";

        feedbackList.forEach(item => {

            const row = document.createElement("tr");

            const values = [
                item.feedbackId,
                item.customer || "Unknown",
                item.parkingName || "Unknown parking",
                "★".repeat(Number(item.rating)) +
                    "☆".repeat(5 - Number(item.rating)),
                item.comments || "No comments",
                item.date || "-"
            ];

            values.forEach((value, index) => {

                const cell = document.createElement("td");
                cell.textContent = value;

                if (index === 3) {
                    cell.className = "rating";
                }

                if (index === 4) {
                    cell.className = "comment-cell";
                }

                row.appendChild(cell);
            });

            feedbackTable.appendChild(row);
        });

        message.textContent =
            "Successfully loaded " + feedbackList.length + " feedback record(s).";

    } catch (error) {

        console.error("Feedback loading error:", error);

        feedbackTable.innerHTML = `
            <tr>
                <td colspan="6" class="empty-row">
                    ${error.message}
                </td>
            </tr>
        `;

        message.textContent = error.message;
    }
}

document.getElementById("refreshBtn").addEventListener(
    "click",
    loadFeedback
);

loadFeedback();
