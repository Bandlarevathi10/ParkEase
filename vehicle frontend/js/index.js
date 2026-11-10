
// ==============================
// MOBILE NAVIGATION
// ==============================

const menuToggle = document.getElementById("menuToggle");
const navLinks = document.getElementById("navLinks");

menuToggle.addEventListener("click", function () {

    navLinks.classList.toggle("show");

    const isOpen = navLinks.classList.contains("show");

    menuToggle.textContent = isOpen ? "✕" : "☰";
    menuToggle.setAttribute("aria-expanded", isOpen);

});


// Close mobile menu after clicking a navigation link

const navigationItems = document.querySelectorAll(".nav-links a");

navigationItems.forEach(function (link) {

    link.addEventListener("click", function () {

        navLinks.classList.remove("show");

        menuToggle.textContent = "☰";
        menuToggle.setAttribute("aria-expanded", "false");

    });

});


// ==============================
// ACTIVE NAVIGATION LINK
// ==============================

navigationItems.forEach(function (link) {

    link.addEventListener("click", function () {

        navigationItems.forEach(function (item) {
            item.classList.remove("active");
        });

        link.classList.add("active");

    });

});


// ==============================
// CURRENT YEAR IN FOOTER
// ==============================

const currentYear = document.getElementById("currentYear");

currentYear.textContent = new Date().getFullYear();


// ==============================
// CONTACT FORM VALIDATION
// ==============================

const contactForm = document.getElementById("contactForm");
const contactStatus = document.getElementById("contactStatus");

contactForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const name = document.getElementById("contactName").value.trim();
    const email = document.getElementById("contactEmail").value.trim();
    const message = document.getElementById("contactMessage").value.trim();

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (name.length < 3) {

        contactStatus.style.color = "#DC2626";
        contactStatus.textContent = "Please enter a name with at least 3 characters.";
        return;

    }

    if (!emailPattern.test(email)) {

        contactStatus.style.color = "#DC2626";
        contactStatus.textContent = "Please enter a valid email address.";
        return;

    }

    if (message.length < 10) {

        contactStatus.style.color = "#DC2626";
        contactStatus.textContent = "Please enter a message with at least 10 characters.";
        return;

    }

    // Frontend demonstration only.
    // Later, this form will send data to a Java Servlet.

    contactStatus.style.color = "#16A34A";
    contactStatus.textContent =
        "Thank you, " + name + "! Your form passed validation. Backend submission will be added later.";

    contactForm.reset();

});


const primaryButtons = document.querySelectorAll(".btn-primary");

primaryButtons.forEach(function (button) {

    button.addEventListener("mouseenter", function () {
        button.style.transition = "0.25s";
    });

});
