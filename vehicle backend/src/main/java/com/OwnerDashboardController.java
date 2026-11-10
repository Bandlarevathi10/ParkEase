
package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pureconnect.mycon;

public class OwnerDashboardController extends HttpServlet {

    private void setCors(HttpServletRequest request,
                         HttpServletResponse response) {

        String origin = request.getHeader("Origin");

        if ("http://localhost:5501".equals(origin)
                || "http://127.0.0.1:5501".equals(origin)) {

            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) {

        setCors(request, response);
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setContentType("text/html;charset=UTF-8");
     
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null
                || !"OWNER".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Please login as an owner.");
            return;
        }

        int ownerId;

        try {
            ownerId = Integer.parseInt(
                    String.valueOf(session.getAttribute("userId")));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Invalid owner session.");
            return;
        }
        String ownerName = (String) session.getAttribute("userName");

        if (ownerName == null || ownerName.trim().isEmpty()) {
            ownerName = "Parking Owner";
        }

        // Load payments when Payments page requests this action
        if ("payments".equalsIgnoreCase(request.getParameter("action"))) {
            loadOwnerPayments(response, ownerId);
            return;
        }

        int totalParking = 0;
        int availableSlots = 0;
        int bookedSlots = 0;
        int maintenanceSlots = 0;
        double totalEarnings = 0.0;
        StringBuilder rows = new StringBuilder();

        try (Connection con = mycon.myownconnection()) {

            // 1. Total parking locations from BOTH databases

        	String parkingSql =
        		    "SELECT COUNT(*) AS totalParking " +
        		    "FROM owner_db.parking WHERE owner_id = ?";

        		try (PreparedStatement ps = con.prepareStatement(parkingSql)) {
        		    ps.setInt(1, ownerId);

        		    try (ResultSet rs = ps.executeQuery()) {
        		        if (rs.next()) {
        		            totalParking = rs.getInt("totalParking");
        		        }
        		    }
        		}

            // 2. Slot counts from BOTH databases


        		String slotSql =
        			    "SELECT " +
        			    "COALESCE(SUM(CASE WHEN UPPER(s.status) = 'AVAILABLE' " +
        			    "THEN 1 ELSE 0 END), 0) AS availableSlots, " +

        			    "COALESCE(SUM(CASE WHEN UPPER(s.status) = 'BOOKED' " +
        			    "THEN 1 ELSE 0 END), 0) AS bookedSlots, " +

        			    "COALESCE(SUM(CASE WHEN UPPER(s.status) = 'MAINTENANCE' " +
        			    "THEN 1 ELSE 0 END), 0) AS maintenanceSlots " +

        			    "FROM owner_db.slots s " +
        			    "JOIN owner_db.parking p ON s.parking_id = p.parking_id " +
        			    "WHERE p.owner_id = ?";

        			try (PreparedStatement ps = con.prepareStatement(slotSql)) {
        			    ps.setInt(1, ownerId);

        			    try (ResultSet rs = ps.executeQuery()) {
        			        if (rs.next()) {
        			            availableSlots = rs.getInt("availableSlots");
        			            bookedSlots = rs.getInt("bookedSlots");
        			            maintenanceSlots = rs.getInt("maintenanceSlots");
        			        }
        			    }
        			}

            // 3. Successful earnings from the owner's parking locations

            String earningsSql =
                    "SELECT COALESCE(SUM(pay.amount), 0) AS earnings " +
                    "FROM payments pay " +
                    "JOIN bookings b ON pay.booking_id = b.booking_id " +

                    "LEFT JOIN parking vp " +
                    "ON b.parking_id = vp.parking_id " +
                    "AND b.parking_source = 'VEHICLE_DB' " +

                    "LEFT JOIN owner_db.parking op " +
                    "ON b.parking_id = op.parking_id " +
                    "AND b.parking_source = 'OWNER_DB' " +

                    "WHERE COALESCE(vp.owner_id, op.owner_id) = ? " +
                    "AND UPPER(pay.payment_status) " +
                    "IN ('SUCCESS', 'PAID', 'COMPLETED')";

            try (PreparedStatement ps = con.prepareStatement(earningsSql)) {

                ps.setInt(1, ownerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalEarnings = rs.getDouble("earnings");
                    }
                }
            }

            // 4. Latest real customer bookings

            String bookingSql =
                    "SELECT b.booking_id, u.name AS customer_name, " +
                    "v.vehicle_number, v.vehicle_type, " +
                    "COALESCE(vp.parking_name, op.parking_name) AS parking_name, " +
                    "b.total_amount, b.booking_status " +

                    "FROM bookings b " +

                    "LEFT JOIN users u ON b.user_id = u.user_id " +
                    "LEFT JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +

                    "LEFT JOIN parking vp " +
                    "ON b.parking_id = vp.parking_id " +
                    "AND b.parking_source = 'VEHICLE_DB' " +

                    "LEFT JOIN owner_db.parking op " +
                    "ON b.parking_id = op.parking_id " +
                    "AND b.parking_source = 'OWNER_DB' " +

                    "WHERE COALESCE(vp.owner_id, op.owner_id) = ? " +
                    "ORDER BY b.booking_date DESC, b.booking_id DESC " +
                    "LIMIT 5";

            try (PreparedStatement ps = con.prepareStatement(bookingSql)) {

                ps.setInt(1, ownerId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        String status = rs.getString("booking_status");

                        if (status == null) {
                            status = "Unknown";
                        }

                        String statusClass = status.toLowerCase()
                                .replaceAll("[^a-z]", "");

                        String vehicle = rs.getString("vehicle_number");

                        if (vehicle == null || vehicle.isEmpty()) {
                            vehicle = rs.getString("vehicle_type");
                        } else if (rs.getString("vehicle_type") != null) {
                            vehicle += " (" + rs.getString("vehicle_type") + ")";
                        }

                        rows.append("<tr>");

                        rows.append("<td>")
                                .append(rs.getInt("booking_id"))
                                .append("</td>");

                        rows.append("<td>")
                                .append(escape(rs.getString("customer_name")))
                                .append("</td>");

                        rows.append("<td>")
                                .append(escape(vehicle))
                                .append("</td>");

                        rows.append("<td>")
                                .append(escape(rs.getString("parking_name")))
                                .append("</td>");

                        rows.append("<td>₹")
                                .append(rs.getBigDecimal("total_amount") == null
                                        ? "0.00"
                                        : rs.getBigDecimal("total_amount").toPlainString())
                                .append("</td>");

                        rows.append("<td><span class='status ")
                                .append(escape(statusClass))
                                .append("'>")
                                .append(escape(status))
                                .append("</span></td>");

                        rows.append("</tr>");
                    }
                }
            }

            if (rows.length() == 0) {
                rows.append(
                        "<tr><td colspan='6'>No bookings found</td></tr>");
            }


            PrintWriter out = response.getWriter();

            out.println("<div id='ownerDashboardData' "
                    + "data-owner-name='" + escape(ownerName) + "' "
                    + "data-total-parking='" + totalParking + "' "
                    + "data-available-slots='" + availableSlots + "' "
                    + "data-booked-slots='" + bookedSlots + "' "
                    + "data-maintenance-slots='" + maintenanceSlots + "' "
                    + "data-total-earnings='" + totalEarnings + "'>");

            out.println("<table><tbody id='bookingRows'>");
            out.println(rows);
            out.println("</tbody></table>");

            out.println("</div>");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().print("Unable to load owner dashboard.");
        }
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
    private void loadOwnerPayments(HttpServletResponse response, int ownerId)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String sql =
            "SELECT pay.payment_id, b.booking_id, " +
            "u.name AS customer_name, v.vehicle_number, " +
            "COALESCE(vp.parking_name, op.parking_name) AS parking_name, " +
            "pay.amount, pay.payment_method, pay.transaction_id, " +
            "pay.payment_status, pay.payment_date " +
            "FROM vehicle.payments pay " +
            "JOIN vehicle.bookings b ON pay.booking_id = b.booking_id " +
            "LEFT JOIN vehicle.users u ON b.user_id = u.user_id " +
            "LEFT JOIN vehicle.vehicles v ON b.vehicle_id = v.vehicle_id " +
            "LEFT JOIN vehicle.parking vp " +
            "ON b.parking_id = vp.parking_id " +
            "AND b.parking_source = 'VEHICLE_DB' " +
            "LEFT JOIN owner_db.parking op " +
            "ON b.parking_id = op.parking_id " +
            "AND b.parking_source = 'OWNER_DB' " +
            "WHERE COALESCE(vp.owner_id, op.owner_id) = ? " +
            "ORDER BY pay.payment_date DESC";

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    if (!first) {
                        json.append(",");
                    }
                    first = false;

                    String customer = rs.getString("customer_name");
                    String parking = rs.getString("parking_name");
                    String method = rs.getString("payment_method");
                    String transactionId = rs.getString("transaction_id");
                    String status = rs.getString("payment_status");

                    java.sql.Timestamp timestamp =
                        rs.getTimestamp("payment_date");

                    String date = timestamp == null
                        ? ""
                        : timestamp.toString();

                    java.math.BigDecimal amount =
                        rs.getBigDecimal("amount");

                    json.append("{")
                        .append("\"paymentId\":").append(rs.getInt("payment_id")).append(",")
                        .append("\"bookingId\":").append(rs.getInt("booking_id")).append(",")
                        .append("\"customer\":\"").append(jsonEscape(customer)).append("\",")
                        .append("\"vehicle\":\"").append(jsonEscape(rs.getString("vehicle_number"))).append("\",")
                        .append("\"parking\":\"").append(jsonEscape(parking)).append("\",")
                        .append("\"amount\":").append(amount == null ? "0" : amount.toPlainString()).append(",")
                        .append("\"method\":\"").append(jsonEscape(method)).append("\",")
                        .append("\"transactionId\":\"").append(jsonEscape(transactionId)).append("\",")
                        .append("\"date\":\"").append(jsonEscape(date)).append("\",")
                        .append("\"status\":\"").append(jsonEscape(status)).append("\"")
                        .append("}");
                }
            }

            json.append("]");
            response.getWriter().print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Unable to load payments\"}");
        }
    }

    private String jsonEscape(String value) {
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
