
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");

const togglePassword = document.getElementById("togglePassword");
const message = document.getElementById("message");

// Show and Hide Password
togglePassword.addEventListener("click", function () {

    if (password.type === "password") {
        password.type = "text";
        confirmPassword.type = "text";
        togglePassword.textContent = "Hide";
    }
    else {
        password.type = "password";
        confirmPassword.type = "password";
        togglePassword.textContent = "Show";
    }

});

// Main validation function
function validateRegister() {

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const role = document.getElementById("role").value;

    const pass = password.value;
    const confirmPass = confirmPassword.value;

    // 1. Empty field validation
    if (name === "" || email === "" || phone === "" ||
        role === "" || pass === "" || confirmPass === "") {

        showMessage("Please fill all the fields!", "red");
        return false;
    }

    // 2. Name minimum length
    if (name.length < 3) {
        showMessage("Name must contain at least 3 characters!", "red");
        return false;
    }

    // 3. Name should contain only alphabets and spaces
    if (!/^[A-Za-z ]+$/.test(name)) {
        showMessage("Name should contain only letters and spaces!", "red");
        return false;
    }

    // 4. Email validation
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showMessage("Enter a valid email address!", "red");
        return false;
    }

    // 5. Phone number validation
    if (!/^[0-9]{10}$/.test(phone)) {
        showMessage("Phone number must contain exactly 10 digits!", "red");
        return false;
    }

    // 6. Phone number should not start with zero
    if (phone.charAt(0) === "0") {
        showMessage("Phone number should not start with zero!", "red");
        return false;
    }

    // 7. Role validation
    if (role !== "USER" && role !== "OWNER") {
        showMessage("Please select a valid role!", "red");
        return false;
    }

    // 8. Password minimum length
    if (pass.length < 8) {
        showMessage("Password must contain at least 8 characters!", "red");
        return false;
    }

    // 9. Uppercase validation
    if (!/[A-Z]/.test(pass)) {
        showMessage("Password must contain an uppercase letter!", "red");
        return false;
    }

    // 10. Lowercase validation
    if (!/[a-z]/.test(pass)) {
        showMessage("Password must contain a lowercase letter!", "red");
        return false;
    }

    // 11. Number validation
    if (!/[0-9]/.test(pass)) {
        showMessage("Password must contain at least one number!", "red");
        return false;
    }

    // 12. Special character validation
    if (!/[!@#$%^&*(),.?":{}|<>_\-+=]/.test(pass)) {
        showMessage("Password must contain a special character!", "red");
        return false;
    }

    // 13. Password should not contain spaces
    if (/\s/.test(pass)) {
        showMessage("Password should not contain spaces!", "red");
        return false;
    }

    // 14. Confirm password validation
    if (pass !== confirmPass) {
        showMessage("Passwords do not match!", "red");
        return false;
    }

    // 15. Password should not be same as name
    if (pass.toLowerCase() === name.toLowerCase()) {
        showMessage("Password should not be the same as your name!", "red");
        return false;
    }

    showMessage("Registration successful! Redirecting to login...", "green");

return true;
}

// Message display function
function showMessage(text, color) {
    message.style.color = color;
    message.textContent = text;
}
