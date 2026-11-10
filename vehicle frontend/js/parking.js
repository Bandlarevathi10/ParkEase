
const bookNow = document.getElementById("bookNow");

bookNow.addEventListener("click", function () {

    const parkingDetails = {
        name: "City Center Parking",
        location: "Guntur Main Road",
        price: 40,
        slots: 12
    };

    sessionStorage.setItem(
        "selectedParking",
        JSON.stringify(parkingDetails)
    );

    window.location.href = "booking.html";
});
