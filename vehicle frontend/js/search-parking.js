
const searchForm = document.getElementById("searchForm");
const searchMessage = document.getElementById("searchMessage");

const priceFilter = document.getElementById("priceFilter");
const parkingCards = document.querySelectorAll(".parking-card");
const noResults = document.getElementById("noResults");

const dateInput = document.getElementById("date");

// Set today's date as the minimum selectable date.
const today = new Date();
const year = today.getFullYear();
const month = String(today.getMonth() + 1).padStart(2, "0");
const day = String(today.getDate()).padStart(2, "0");

dateInput.min = `${year}-${month}-${day}`;

searchForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const location = document.getElementById("location").value.trim();
    const date = dateInput.value;
    const time = document.getElementById("time").value;

    if (location === "" || date === "" || time === "") {
        searchMessage.textContent = "Please enter location, date and time.";
        return;
    }

    if (date < dateInput.min) {
        searchMessage.textContent = "Please select today or a future date.";
        return;
    }

    searchMessage.textContent =
        "Search details entered. Connect the backend to retrieve available parking spaces.";
});

priceFilter.addEventListener("change", function () {

    const selectedPrice = priceFilter.value;
    let visibleCount = 0;

    parkingCards.forEach(function (card) {

        const price = Number(card.dataset.price);
        let shouldShow = false;

        if (selectedPrice === "all") {
            shouldShow = true;
        } else if (selectedPrice === "low" && price < 50) {
            shouldShow = true;
        } else if (selectedPrice === "medium" &&
                   price >= 50 && price <= 100) {
            shouldShow = true;
        } else if (selectedPrice === "high" && price > 100) {
            shouldShow = true;
        }

        card.style.display = shouldShow ? "block" : "none";

        if (shouldShow) {
            visibleCount++;
        }
    });

    noResults.style.display = visibleCount === 0 ? "block" : "none";
});
if (sessionStorage.getItem("loggedIn") !== "true") {
    alert("Please login first!");
    window.location.href = "index.html";
}