package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pureconnect.mycon;

public class DashboardControllerr extends HttpServlet {
	 private static final long serialVersionUID = 1L;
	// Allow requests from your VS Code Live Server
	    private void setCors(HttpServletResponse response) {
	        response.setHeader("Access-Control-Allow-Origin",
	                           "http://localhost:5501");
	        response.setHeader("Access-Control-Allow-Credentials", "true");
	        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
	        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
	    }

	    @Override
	    protected void doOptions(HttpServletRequest request,
	                             HttpServletResponse response) {
	        setCors(response);
	        response.setStatus(HttpServletResponse.SC_OK);
	    }

	    @Override
	    protected void doGet(HttpServletRequest request,
	                         HttpServletResponse response)
	                         throws ServletException, IOException {

	        setCors(response);
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        PrintWriter out = response.getWriter();

	        HttpSession session = request.getSession(false);

	        // Check whether the user is logged in
	        if (session == null || session.getAttribute("userId") == null) {
	            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            out.print("{\"success\":false,\"message\":\"Please login again\"}");
	            return;
	        }

	        int userId = ((Number) session.getAttribute("userId")).intValue();

	        String userName = "";
	        int totalBookings = 0;
	        int activeBookings = 0;
	        int availableSlots = 0;
	        int totalVehicles = 0;

	        StringBuilder bookingsJson = new StringBuilder();

	        String userSql =
	            "SELECT name FROM users WHERE user_id = ?";

	        String bookingCountSql =
	            "SELECT COUNT(*) AS total, " +
	            "SUM(CASE WHEN LOWER(booking_status) " +
	            "IN ('pending','confirmed','active') THEN 1 ELSE 0 END) AS active " +
	            "FROM bookings WHERE user_id = ?";

	        String vehicleSql =
	            "SELECT COUNT(*) FROM vehicles WHERE user_id = ?";

	        String slotsSql =
	            "SELECT COUNT(*) FROM slots s " +
	            "JOIN parking p ON s.parking_id = p.parking_id " +
	            "WHERE LOWER(s.status) = 'available' " +
	            "AND LOWER(p.status) = 'active'";

	        String recentSql =
	            "SELECT b.booking_id, p.parking_name, " +
	            "v.vehicle_type, b.start_time, b.total_amount, " +
	            "b.booking_status " +
	            "FROM bookings b " +
	            "LEFT JOIN parking p ON b.parking_id = p.parking_id " +
	            "LEFT JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
	            "WHERE b.user_id = ? " +
	            "ORDER BY b.booking_date DESC LIMIT 5";

	        try (Connection con = mycon.myownconnection()) {

	            // 1. Get user name
	            try (PreparedStatement ps = con.prepareStatement(userSql)) {
	                ps.setInt(1, userId);

	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        userName = rs.getString("name");
	                    }
	                }
	            }

	            // 2. Get booking counts
	            try (PreparedStatement ps = con.prepareStatement(bookingCountSql)) {
	                ps.setInt(1, userId);

	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        totalBookings = rs.getInt("total");
	                        activeBookings = rs.getInt("active");
	                    }
	                }
	            }

	            // 3. Get vehicle count
	            try (PreparedStatement ps = con.prepareStatement(vehicleSql)) {
	                ps.setInt(1, userId);

	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        totalVehicles = rs.getInt(1);
	                    }
	                }
	            }

	            // 4. Get available parking slots
	            try (PreparedStatement ps = con.prepareStatement(slotsSql);
	                 ResultSet rs = ps.executeQuery()) {

	                if (rs.next()) {
	                    availableSlots = rs.getInt(1);
	                }
	            }

	            // 5. Get recent bookings
	            try (PreparedStatement ps = con.prepareStatement(recentSql)) {
	                ps.setInt(1, userId);

	                try (ResultSet rs = ps.executeQuery()) {

	                    boolean first = true;

	                    while (rs.next()) {

	                        if (!first) {
	                            bookingsJson.append(",");
	                        }

	                        first = false;

	                        int bookingId = rs.getInt("booking_id");
	                        String location = rs.getString("parking_name");
	                        String vehicle = rs.getString("vehicle_type");
	                        String date = rs.getString("start_time");
	                        String amount = rs.getString("total_amount");
	                        String status = rs.getString("booking_status");

	                        bookingsJson.append("{")
	                            .append("\"bookingId\":").append(bookingId).append(",")
	                            .append("\"location\":\"").append(escape(location)).append("\",")
	                            .append("\"vehicle\":\"").append(escape(vehicle)).append("\",")
	                            .append("\"date\":\"").append(escape(date)).append("\",")
	                            .append("\"amount\":\"").append(escape(amount)).append("\",")
	                            .append("\"status\":\"").append(escape(status)).append("\"")
	                            .append("}");
	                    }
	                }
	            }

	            // Send all dashboard information as JSON
	            String json =
	                "{"
	                + "\"success\":true,"
	                + "\"userName\":\"" + escape(userName) + "\","
	                + "\"totalBookings\":" + totalBookings + ","
	                + "\"activeBookings\":" + activeBookings + ","
	                + "\"availableSlots\":" + availableSlots + ","
	                + "\"totalVehicles\":" + totalVehicles + ","
	                + "\"bookings\":[" + bookingsJson + "]"
	                + "}";

	            out.print(json);

	            System.out.println("Dashboard data loaded for user ID: " + userId);

	        } catch (Exception e) {

	            e.printStackTrace();

	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            out.print("{\"success\":false,\"message\":\"Unable to load dashboard data\"}");
	        }
	    }

	    // Safely escape database text before adding it to JSON
	    private String escape(String value) {

	        if (value == null) {
	            return "";
	        }

	        return value.replace("\\", "\\\\")
	                    .replace("\"", "\\\"")
	                    .replace("\n", "\\n")
	                    .replace("\r", "\\r")
	                    .replace("\t", "\\t");
	    }
}
