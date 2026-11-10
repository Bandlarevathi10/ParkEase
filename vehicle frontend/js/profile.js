const API_URL =
    "http://localhost:8081/vehicle_backend/UserProfileController";

const form = document.getElementById("profileForm");

const nameInput = document.getElementById("name");
const emailInput = document.getElementById("email");
const phoneInput = document.getElementById("phone");
const roleInput = document.getElementById("role");

const displayName = document.getElementById("displayName");
const avatar = document.getElementById("avatar");
const message = document.getElementById("message");

let originalData = {};

document.addEventListener("DOMContentLoaded", loadProfile);

async function loadProfile() {

    try {
        const response = await fetch(API_URL, {
            method: "GET",
            credentials: "include"
        });

        const data = await response.json();

        if (!response.ok || !data.success) {
            message.textContent = data.message || "Unable to load profile";
            return;
        }

        nameInput.value = data.name || "";
        emailInput.value = data.email || "";
        phoneInput.value = data.phone || "";
        roleInput.value = data.role || "";

        displayName.textContent = data.name || "ParkEase User";
        avatar.textContent = data.name
            ? data.name.charAt(0).toUpperCase()
            : "P";

        originalData = {
            name: nameInput.value,
            email: emailInput.value,
            phone: phoneInput.value
        };

        nameInput.readOnly = false;
        emailInput.readOnly = false;
        phoneInput.readOnly = false;

    } catch (error) {
        console.error(error);
        message.textContent = "Server connection failed";
    }
}

form.addEventListener("submit", async function(event) {

    event.preventDefault();

    const formData = new URLSearchParams();

    formData.append("name", nameInput.value.trim());
    formData.append("email", emailInput.value.trim());
    formData.append("phone", phoneInput.value.trim());

    try {

        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        const data = await response.json();

        message.textContent = data.message;

        if (response.ok && data.success) {

            displayName.textContent = nameInput.value;
            avatar.textContent =
                nameInput.value.charAt(0).toUpperCase();

            originalData = {
                name: nameInput.value,
                email: emailInput.value,
                phone: phoneInput.value
            };
        }

    } catch (error) {
        console.error(error);
        message.textContent = "Unable to save profile";
    }
});

document.getElementById("resetBtn").addEventListener("click", function() {

    nameInput.value = originalData.name || "";
    emailInput.value = originalData.email || "";
    phoneInput.value = originalData.phone || "";

    message.textContent = "";
});