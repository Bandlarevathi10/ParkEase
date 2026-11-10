
document.addEventListener("DOMContentLoaded", function () {

    const params = new URLSearchParams(window.location.search);
    const bookingId = params.get("bookingId");

    const bookingIdField = document.getElementById("paymentBookingId");
    const payButton = document.getElementById("payButton");
    const paymentMessage = document.getElementById("paymentMessage");
    const paymentForm = document.getElementById("paymentForm");

    // Read booking ID from the URL
    if (bookingId && /^\d+$/.test(bookingId)) {
        bookingIdField.value = bookingId;
        document.getElementById("parkingName").textContent =
            "Booking #" + bookingId;

        paymentMessage.textContent = "Choose a payment method to continue.";
        payButton.disabled = false;
    } else {
        bookingIdField.value = "";
        payButton.disabled = true;

        paymentMessage.textContent =
            "Booking ID is missing. Please complete your booking first.";

        paymentMessage.style.color = "red";
    }

    // Highlight selected payment method
    const methods = document.querySelectorAll(
        'input[name="paymentMethod"]'
    );

    methods.forEach(function (method) {
        method.addEventListener("change", function () {

            document.querySelectorAll(".method").forEach(function (item) {
                item.classList.remove("active");
            });

            this.closest(".method").classList.add("active");
        });
    });

    // Submit payment to Java servlet
    paymentForm.addEventListener("submit", function (event) {

        const selectedMethod = document.querySelector(
            'input[name="paymentMethod"]:checked'
        );

        if (!bookingId || !/^\d+$/.test(bookingId)) {
            event.preventDefault();
            paymentMessage.textContent =
                "Invalid booking ID. Please complete your booking again.";
            paymentMessage.style.color = "red";
            return;
        }

        if (!selectedMethod) {
            event.preventDefault();
            paymentMessage.textContent =
                "Please select a payment method.";
            paymentMessage.style.color = "red";
            return;
        }

        bookingIdField.value = bookingId;

        // Allow the form to submit to Gateway.
        // PaymentControllerr will save the payment in MySQL.
    });

});
