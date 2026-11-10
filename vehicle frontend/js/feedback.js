
let selectedRating = 0;

const ratingButtons = document.querySelectorAll(".rating button");
const ratingText = document.getElementById("ratingText");
const feedbackForm = document.getElementById("feedbackForm");
const feedbackMessage = document.getElementById("feedbackMessage");
const submitBtn = document.getElementById("submitBtn");

const ratingLabels = {
    1: "Poor",
    2: "Fair",
    3: "Good",
    4: "Very Good",
    5: "Excellent"
};

const FEEDBACK_URL =
    "http://localhost:8081/vehicle_backend/Gateway";

// STAR RATING
ratingButtons.forEach(function(button, index) {

    // Prevent star buttons from submitting the form
    button.type = "button";

    button.addEventListener("click", function() {

        selectedRating = Number(this.dataset.value);

        updateStars();

        ratingText.textContent = ratingLabels[selectedRating];

    });

});

// UPDATE STAR COLORS
function updateStars() {

    ratingButtons.forEach(function(button) {

        const value = Number(button.dataset.value);

        if (value <= selectedRating) {
            button.classList.add("selected");
        } else {
            button.classList.remove("selected");
        }

    });

}

// SUBMIT FEEDBACK
feedbackForm.addEventListener("submit", async function(event) {

    // Stop page refresh and redirect
    event.preventDefault();

    const parkingId = document.getElementById("parkingId").value;
    const comments = document.getElementById("comments").value.trim();

    feedbackMessage.textContent = "";
    feedbackMessage.style.color = "red";

    if (parkingId === "") {
        feedbackMessage.textContent = "Please select a parking location.";
        return;
    }

    if (selectedRating === 0) {
        feedbackMessage.textContent = "Please select your star rating.";
        return;
    }

    if (comments.length < 5) {
        feedbackMessage.textContent =
            "Please enter at least 5 characters in your comments.";
        return;
    }

    if (comments.length > 1000) {
        feedbackMessage.textContent =
            "Comments cannot exceed 1000 characters.";
        return;
    }

    const formData = new URLSearchParams();

    formData.append("action", "Feedback");
    formData.append("feedbackAction", "add");
    formData.append("parkingId", parkingId);
    formData.append("rating", selectedRating);
    formData.append("comments", comments);

    submitBtn.disabled = true;
    submitBtn.textContent = "Submitting...";

    try {

        const response = await fetch(FEEDBACK_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        const result = (await response.text()).trim();

        if (!response.ok) {
            throw new Error(result || "Server error while submitting feedback.");
        }

        if (result === "Feedback submitted successfully.") {

            document.querySelector(".feedback-container").style.display = "none";

            document.getElementById("successSection").style.display = "block";

        } else {

            feedbackMessage.textContent =
                result || "Unable to submit feedback.";

            submitBtn.disabled = false;
            submitBtn.textContent = "Submit Feedback";

        }

    } catch (error) {

        console.error("Feedback submission error:", error);

        feedbackMessage.textContent =
            error.message || "Unable to connect to the server.";

        submitBtn.disabled = false;
        submitBtn.textContent = "Submit Feedback";

    }

});

// GO TO USER DASHBOARD
function goToDashboard() {
    window.location.href = "user-dashboard.html";
}
