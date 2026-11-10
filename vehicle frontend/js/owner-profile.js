document.addEventListener("DOMContentLoaded", function () {

    fetch("http://localhost:8081/vehicle_backend/OwnerProfileController", {
        method: "GET",
        credentials: "include"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Unable to load profile. Please login again.");
        }

        return response.json();
    })
    .then(data => {

        if (data.error) {
            throw new Error(data.error);
        }

        function show(id, value) {
            const element = document.getElementById(id);

            if (element) {
                element.textContent = value || "Not provided";
            }
        }

        show("ownerName", data.name);
        show("ownerEmail", data.email);
        show("name", data.name);
        show("email", data.email);
        show("phone", data.phone);
        show("role", data.role);
        show("ownerRole", data.role);
        show("businessName", data.businessName);
        show("businessAddress", data.businessAddress);
        show("contactPerson", data.contactPerson);
        show("contactPhone", data.contactPhone);

        // First letter of logged-in owner's email
        const initial = data.email
            ? data.email.trim().charAt(0).toUpperCase()
            : "O";

        show("ownerInitial", initial);
        show("largeInitial", initial);

    })
    .catch(error => {
        console.error("Owner Profile Error:", error);
        showError(error.message);
    });

    function showError(message) {
        document.getElementById("profileMessage").textContent = message;
    }

});