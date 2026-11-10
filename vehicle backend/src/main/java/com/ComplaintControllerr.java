package com;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pureconnect.mycon;


public class ComplaintControllerr extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    // Display normal text messages
    private void sendMessage(HttpServletResponse response, String message)
            throws IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(message);
    }
    private void setCors(HttpServletRequest request, HttpServletResponse response) {

        String origin = request.getHeader("Origin");

        if ("http://localhost:5501".equals(origin) ||
            "http://127.0.0.1:5501".equals(origin)) {

            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
    }
    // Escape text before displaying it in HTML
    private String escapeHTML(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    // USER SUBMITS COMPLAINT
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
    	setCors(request, response);
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            sendMessage(response, "Please login first.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String action = request.getParameter("complaintAction");

        // ADD COMPLAINT
        if ("add".equals(action)) {

            String subject = request.getParameter("subject");
            String description = request.getParameter("description");
            String bookingValue = request.getParameter("bookingId");

            if (subject == null || subject.trim().isEmpty()
                    || description == null || description.trim().isEmpty()) {

                sendMessage(response, "Subject and description are required.");
                return;
            }

            Integer bookingId = null;

            try {

                if (bookingValue != null && !bookingValue.trim().isEmpty()) {
                    bookingId = Integer.parseInt(bookingValue);

                    if (bookingId <= 0) {
                        sendMessage(response, "Invalid booking ID.");
                        return;
                    }
                }

                try (Connection con = mycon.myownconnection()) {

                    // Check that the booking belongs to this user
                    if (bookingId != null) {

                        String checkSql = "SELECT booking_id FROM bookings "
                                        + "WHERE booking_id = ? AND user_id = ?";

                        try (PreparedStatement check = con.prepareStatement(checkSql)) {

                            check.setInt(1, bookingId);
                            check.setInt(2, userId);

                            try (ResultSet rs = check.executeQuery()) {

                                if (!rs.next()) {
                                    sendMessage(response,
                                            "Invalid booking. Select your own booking.");
                                    return;
                                }
                            }
                        }
                    }

                    String sql = "INSERT INTO complaints (user_id, booking_id, subject, description) VALUES (?, ?, ?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);

                    ps.setInt(1, userId);

                    if (bookingId == null) {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(2, bookingId);
                    }

                    ps.setString(3, subject);
                    ps.setString(4, description);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        ResultSet keys = ps.getGeneratedKeys();

                        if (keys.next()) {
                            int complaintId = keys.getInt(1);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().print("SUCCESS|" + complaintId);
                        } else {
                            response.getWriter().print("ERROR|Complaint saved, but ID could not be retrieved.");
                        }

                        keys.close();
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().print("ERROR|Complaint could not be saved.");
                    }
                }

            } catch (NumberFormatException e) {

                sendMessage(response, "Invalid booking ID.");

            } catch (Exception e) {

                e.printStackTrace();
                sendMessage(response,
                        "Database error while submitting complaint.");
            }
        }

        // OWNER UPDATES COMPLAINT STATUS
        else if ("updateStatus".equals(action)) {

            String complaintValue = request.getParameter("complaintId");
            String status = request.getParameter("complaintStatus");

            if (complaintValue == null || status == null) {
                sendMessage(response, "Complaint ID and status are required.");
                return;
            }

            if (!"OPEN".equals(status)
                    && !"IN_PROGRESS".equals(status)
                    && !"RESOLVED".equals(status)
                    && !"CLOSED".equals(status)) {

                sendMessage(response, "Invalid complaint status.");
                return;
            }

            try {

                int complaintId = Integer.parseInt(complaintValue);

                String sql = "UPDATE complaints c "
                           + "JOIN bookings b ON c.booking_id = b.booking_id "
                           + "JOIN parking p ON b.parking_id = p.parking_id "
                           + "SET c.complaint_status = ? "
                           + "WHERE c.complaint_id = ? AND p.owner_id = ?";

                try (Connection con = mycon.myownconnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, status);
                    ps.setInt(2, complaintId);
                    ps.setInt(3, userId);

                    int result = ps.executeUpdate();

                    if (result > 0) {
                        sendMessage(response,
                                "Complaint status updated successfully.");
                    } else {
                        sendMessage(response,
                                "Complaint not found or you cannot update it.");
                    }
                }

            } catch (NumberFormatException e) {

                sendMessage(response, "Invalid complaint ID.");

            } catch (Exception e) {

                e.printStackTrace();
                sendMessage(response, "Unable to update complaint status.");
            }
        }

        else {
            sendMessage(response, "Invalid complaint action.");
        }
    }

    // USER AND OWNER VIEW COMPLAINTS
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
    	setCors(request, response);
     
        System.out.println("COMPLAINT CONTROLLER REACHED");

        HttpSession session = request.getSession(false);

        System.out.println("SESSION: " + session);

        if (session != null) {
            System.out.println("USER ID: " + session.getAttribute("userId"));
            System.out.println("USER ROLE: " + session.getAttribute("userRole"));
        }

        System.out.println("COMPLAINT ACTION: " +
            request.getParameter("complaintAction"));
        if (session == null || session.getAttribute("userId") == null) {
            sendMessage(response, "Please login first.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String action = request.getParameter("complaintAction");
        System.out.println("GATEWAY ACTION: " + action);
        System.out.println("COMPLAINT ACTION: " +
            request.getParameter("complaintAction"));
        System.out.println("SUBJECT: " +
            request.getParameter("subject"));
        String sql;

        // USER VIEWS THEIR COMPLAINTS
        if ("viewUser".equals(action)) {

            sql = "SELECT complaint_id, user_id, booking_id, subject, "
                + "description, complaint_status, complaint_date "
                + "FROM complaints "
                + "WHERE user_id = ? "
                + "ORDER BY complaint_date DESC";
        }

        // OWNER VIEWS COMPLAINTS FOR THEIR PARKING LOCATIONS
        else if ("viewOwner".equals(action)) {

            sql = "SELECT DISTINCT c.complaint_id, c.user_id, c.booking_id, "
                + "c.subject, c.description, c.complaint_status, c.complaint_date "
                + "FROM complaints c "
                + "JOIN bookings b ON c.booking_id = b.booking_id "
                + "JOIN parking p ON b.parking_id = p.parking_id "
                + "WHERE p.owner_id = ? "
                + "ORDER BY c.complaint_date DESC";
        }

        else {
            sendMessage(response, "Invalid complaint action.");
            return;
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                java.io.PrintWriter out = response.getWriter();

                out.println("<html><head><title>Complaints</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;padding:20px;}");
                out.println("table{border-collapse:collapse;width:100%;}");
                out.println("th,td{border:1px solid #ccc;padding:10px;text-align:center;}");
                out.println("th{background:#333;color:white;}");
                out.println("</style></head><body>");

                out.println("<h2>Complaint Details</h2>");

                out.println("<table>");

                out.println("<tr>");
                out.println("<th>Complaint ID</th>");
                out.println("<th>User ID</th>");
                out.println("<th>Booking ID</th>");
                out.println("<th>Subject</th>");
                out.println("<th>Description</th>");
                out.println("<th>Status</th>");
                out.println("<th>Date</th>");
                out.println("</tr>");

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    int bookingId = rs.getInt("booking_id");
                    boolean bookingIsNull = rs.wasNull();

                    out.println("<tr>");

                    out.println("<td>" + rs.getInt("complaint_id") + "</td>");
                    out.println("<td>" + rs.getInt("user_id") + "</td>");

                    out.println("<td>"
                            + (bookingIsNull ? "N/A" : bookingId)
                            + "</td>");

                    out.println("<td>"
                            + escapeHTML(rs.getString("subject"))
                            + "</td>");

                    out.println("<td>"
                            + escapeHTML(rs.getString("description"))
                            + "</td>");

                    out.println("<td>"
                            + escapeHTML(rs.getString("complaint_status"))
                            + "</td>");

                    out.println("<td>"
                            + rs.getTimestamp("complaint_date")
                            + "</td>");

                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='7'>No complaints found.</td></tr>");
                }

                out.println("</table>");
                out.println("</body></html>");
            }

        } catch (Exception e) {

            e.printStackTrace();
            sendMessage(response, "Unable to load complaints.");
        }
    }
}


