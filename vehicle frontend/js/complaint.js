
console.log("COMPLAINT JS LOADED");

const complaintForm = document.getElementById("complaintForm");

complaintForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const message = document.getElementById("complaintMessage");

    const subject = document.getElementById("subject").value;
    const bookingId = document.getElementById("bookingId").value.trim();
    const description = document.getElementById("description").value.trim();

    if (!subject || !description) {
        message.textContent = "Please fill in all required fields.";
        return;
    }

    const data = new URLSearchParams();

    data.append("action", "Complaint");
    data.append("complaintAction", "add");
    data.append("subject", subject);
    data.append("bookingId", bookingId);
    data.append("description", description);

    console.log("SENDING:", data.toString());

    try {

        message.textContent = "Submitting complaint...";

        const response = await fetch(
            "http://localhost:8081/vehicle_backend/Gateway",
            {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: data.toString()
            }
        );

        const result = (await response.text()).trim();

        console.log("SERVER RESPONSE:", result);

        if (!response.ok || !result.startsWith("SUCCESS|")) {
            message.textContent = result || "Complaint submission failed.";
            return;
        }

        const complaintId = result.split("|")[1];

        document.getElementById("successDetails").textContent =
            "Your complaint ID is CMP" + complaintId;

        document.querySelector(".complaint-layout").style.display = "none";

        document.getElementById("successSection").style.display = "block";

        complaintForm.reset();

    } catch (error) {

        console.error("Complaint error:", error);

        message.textContent = "Unable to connect to the server.";

    }

});

function closeSuccess() {

    document.getElementById("successSection").style.display = "none";

    document.querySelector(".complaint-layout").style.display = "grid";

}
