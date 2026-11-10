package com;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pureconnect.mycon;


public class PaymentControllerr extends HttpServlet {
	 private static final long serialVersionUID = 1L;

	    @Override
	    protected void doGet(HttpServletRequest request,
	                         HttpServletResponse response)
	            throws ServletException, IOException {

	        response.sendError(
	            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
	            "Please submit payment using the payment form."
	        );
	    }

	    @Override
	    protected void doPost(HttpServletRequest request,
	                          HttpServletResponse response)
	            throws ServletException, IOException {

	        HttpSession session = request.getSession(false);

	        if (session == null || session.getAttribute("userId") == null) {
	            response.sendRedirect(
	                request.getContextPath() + "/login.html"
	            );
	            return;
	        }

	        int userId = Integer.parseInt(
	            session.getAttribute("userId").toString()
	        );

	        String bookingIdValue = request.getParameter("bookingId");
	        String paymentMethod = request.getParameter("paymentMethod");

	        if (bookingIdValue == null || !bookingIdValue.matches("\\d+")
	                || paymentMethod == null) {

	            response.sendRedirect(
	                request.getContextPath() + "/payment.html?status=invalid"
	            );
	            return;
	        }

	        if (!paymentMethod.equals("UPI")
	                && !paymentMethod.equals("CARD")
	                && !paymentMethod.equals("NET_BANKING")
	                && !paymentMethod.equals("CASH")) {

	            response.sendError(
	                HttpServletResponse.SC_BAD_REQUEST,
	                "Invalid payment method."
	            );
	            return;
	        }

	        int bookingId = Integer.parseInt(bookingIdValue);

	        Connection con = null;

	        try {

	            con = mycon.myownconnection();
	            con.setAutoCommit(false);

	            double amount = 0;

	            // 1. Verify booking belongs to logged-in user
	            String bookingQuery =
	                "SELECT total_amount, booking_status " +
	                "FROM bookings WHERE booking_id=? AND user_id=? FOR UPDATE";

	            try (PreparedStatement ps =
	                    con.prepareStatement(bookingQuery)) {

	                ps.setInt(1, bookingId);
	                ps.setInt(2, userId);

	                try (ResultSet rs = ps.executeQuery()) {

	                    if (!rs.next()) {
	                        con.rollback();

	                        response.sendError(
	                            HttpServletResponse.SC_FORBIDDEN,
	                            "Booking not found or does not belong to you."
	                        );
	                        return;
	                    }

	                    amount = rs.getDouble("total_amount");
	                }
	            }

	            // 2. Check whether payment already exists
	            String checkPayment =
	                "SELECT payment_id FROM payments WHERE booking_id=?";

	            try (PreparedStatement ps =
	                    con.prepareStatement(checkPayment)) {

	                ps.setInt(1, bookingId);

	                try (ResultSet rs = ps.executeQuery()) {

	                    if (rs.next()) {
	                        con.rollback();

	                        response.sendRedirect(
	                            request.getContextPath()
	                            + "/payment.html?bookingId="
	                            + bookingId + "&status=alreadyPaid"
	                        );
	                        return;
	                    }
	                }
	            }

	            // 3. Generate demo transaction ID
	            String transactionId =
	                "PARK-" + UUID.randomUUID().toString()
	                .substring(0, 8).toUpperCase();

	            // 4. Insert payment
	            String insertPayment =
	                "INSERT INTO payments " +
	                "(booking_id, amount, payment_method, transaction_id, payment_status, payment_date) " +
	                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

	            try (PreparedStatement ps =
	                    con.prepareStatement(insertPayment)) {

	                ps.setInt(1, bookingId);
	                ps.setDouble(2, amount);
	                ps.setString(3, paymentMethod);
	                ps.setString(4, transactionId);

	                // Simulated success for this academic project
	                ps.setString(5, "SUCCESS");

	                ps.executeUpdate();
	            }

	            // 5. Update booking status
	            String updateBooking =
	                "UPDATE bookings SET booking_status='CONFIRMED' " +
	                "WHERE booking_id=? AND user_id=?";

	            try (PreparedStatement ps =
	                    con.prepareStatement(updateBooking)) {

	                ps.setInt(1, bookingId);
	                ps.setInt(2, userId);

	                ps.executeUpdate();
	            }

	            con.commit();

	            // 6. Show payment success page
	            response.setContentType("text/html;charset=UTF-8");

	            response.getWriter().println(
	                "<!DOCTYPE html><html><head><title>Payment Successful</title>"
	                + "<style>"
	                + "body{font-family:Arial;text-align:center;background:#eef4fa;padding:50px;}"
	                + ".box{background:white;padding:35px;max-width:450px;margin:auto;"
	                + "border-radius:12px;box-shadow:0 5px 20px #ccc;}"
	                + "h1{color:green;}"
	                + "</style></head><body>"
	                + "<div class='box'>"
	                + "<h1>Payment Successful!</h1>"
	                + "<p>Your parking booking is confirmed.</p>"
	                + "<p>Booking ID: " + bookingId + "</p>"
	                + "<p>Amount: Rs. " + amount + "</p>"
	                + "<p>Payment Method: " + paymentMethod + "</p>"
	                + "<p>Transaction ID: " + transactionId + "</p>"
	                +  "<a href='http://localhost:5501/user-dashboard.html'>Back to DashBoard</a>"
	                + "</div></body></html>"
	            );

	        } catch (Exception e) {

	            try {
	                if (con != null) {
	                    con.rollback();
	                }
	            } catch (Exception rollbackError) {
	                rollbackError.printStackTrace();
	            }

	            e.printStackTrace();

	            response.sendError(
	                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                "Payment failed. Please check the server console."
	            );

	        } finally {

	            try {
	                if (con != null) {
	                    con.setAutoCommit(true);
	                    con.close();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
}
