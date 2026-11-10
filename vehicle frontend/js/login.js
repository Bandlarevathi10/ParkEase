function validateLogin() {

    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value.trim();

    if (email === "" || password === "") {
        alert("Please enter email and password");
        return false;
    }

    if (!email.includes("@")) {
        alert("Enter a valid email");
        return false;
    }

    return true;
}


// SHOW / HIDE PASSWORD
document.getElementById("togglePassword").addEventListener("click", function () {

    let password = document.getElementById("password");

    if (password.type === "password") {
        password.type = "text";
        this.textContent = "Hide";
    } else {
        password.type = "password";
        this.textContent = "Show";
    }

});