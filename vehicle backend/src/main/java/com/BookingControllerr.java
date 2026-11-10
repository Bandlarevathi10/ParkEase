package com;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.BookingData;
import com.model.service.UserServicee;


public class BookingControllerr extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserServicee service = new UserServicee();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Please submit the booking form."
        );
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
throws ServletException, IOException {

request.setCharacterEncoding("UTF-8");

HttpSession session = request.getSession(false);

System.out.println("----- BOOKING SESSION CHECK -----");
System.out.println("Session exists: " + (session != null));

if (session == null || session.getAttribute("userId") == null) {
    response.sendRedirect(
        "http://localhost:5501/login.html?status=loginrequired"
    );
    return;
}

try {
// STEP 2: Get logged-in user ID
int userId = Integer.parseInt(
  session.getAttribute("userId").toString()
);

// STEP 3: Get form values
String parkingIdText = request.getParameter("parkingId");
String slotIdText = request.getParameter("slotId");
String vehicleNumber = request.getParameter("vehicleNumber");
String vehicleModel = request.getParameter("vehicleModel");
String vehicleType = request.getParameter("vehicleType");
String parkingDate = request.getParameter("parkingDate");
String startTime = request.getParameter("startTime");
String endTime = request.getParameter("endTime");

System.out.println("----- BOOKING FORM DATA -----");
System.out.println("User ID: " + userId);
System.out.println("Parking ID: " + parkingIdText);
System.out.println("Slot ID: " + slotIdText);
System.out.println("Vehicle Number: " + vehicleNumber);
System.out.println("Vehicle Model: " + vehicleModel);
System.out.println("Vehicle Type: " + vehicleType);
System.out.println("Date: " + parkingDate);
System.out.println("Start Time: " + startTime);
System.out.println("End Time: " + endTime);

// STEP 4: Validate required fields
if (parkingIdText == null || slotIdText == null ||
  vehicleNumber == null || vehicleModel == null ||
  vehicleType == null || parkingDate == null ||
  startTime == null || endTime == null ||
  parkingIdText.trim().isEmpty() ||
  slotIdText.trim().isEmpty() ||
  vehicleNumber.trim().isEmpty() ||
  vehicleModel.trim().isEmpty() ||
  vehicleType.trim().isEmpty() ||
  parkingDate.trim().isEmpty() ||
  startTime.trim().isEmpty() ||
  endTime.trim().isEmpty()) {

  response.sendRedirect(
      "http://localhost:5501/booking.html?status=empty"
  );
  return;
}

// STEP 5: Convert IDs
int parkingId = Integer.parseInt(parkingIdText);
int slotId = Integer.parseInt(slotIdText);

// STEP 6: Convert date and time
LocalDate date = LocalDate.parse(parkingDate);
LocalTime start = LocalTime.parse(startTime);
LocalTime end = LocalTime.parse(endTime);

Timestamp bookingStart =
  Timestamp.valueOf(date.atTime(start));

Timestamp bookingEnd =
  Timestamp.valueOf(date.atTime(end));

// STEP 7: Validate time
if (!bookingEnd.after(bookingStart)) {
  response.sendRedirect(
      "http://localhost:5501/booking.html?status=invalidtime"
  );
  return;
}

// STEP 8: Create booking object
BookingData booking = new BookingData();

booking.setUserId(userId);
booking.setParkingId(parkingId);
booking.setSlotId(slotId);
booking.setStartTime(bookingStart);
booking.setEndTime(bookingEnd);
booking.setVehicleNumber(
  vehicleNumber.trim().toUpperCase()
);
booking.setVehicleModel(vehicleModel.trim());
booking.setVehicleType(vehicleType.trim());

//STEP 9: Validate slot ID before saving
if (slotIdText == null || !slotIdText.matches("\\d+")) {
 response.sendRedirect(
     "http://localhost:5501/booking.html?status=invalidid"
 );
 return;
}

//STEP 10: Save booking in database
int bookingId = service.createBooking(booking);

System.out.println("Created Booking ID: " + bookingId);
if (bookingId > 0) {

    System.out.println("Booking successful. Redirecting to payment.");

    response.sendRedirect(
        "http://localhost:5501/payment.html?bookingId=" + bookingId
    );

} else {

    System.out.println("Booking failed. Check database.");

    response.sendRedirect(
        "http://localhost:5501/booking.html?status=failed"
    );
}

} catch (NumberFormatException e) {

e.printStackTrace();

response.sendRedirect(
  "http://localhost:5501/booking.html?status=invalidid"
);

} catch (Exception e) {

e.printStackTrace();

response.setContentType("text/html;charset=UTF-8");

response.getWriter().println(
  "<h3>Error while processing booking: " +
  e.getMessage() + "</h3>"
);
}
}
}