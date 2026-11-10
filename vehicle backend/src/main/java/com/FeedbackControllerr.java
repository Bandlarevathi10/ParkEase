package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.FeedBackData;
import com.model.service.UserServicee;
import com.pureconnect.mycon;
import com.pureconnect2.myconnect;


public class FeedbackControllerr extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Display a normal text message
    private void sendMessage(HttpServletResponse response, String message)
            throws IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(message);
    }
    private void setCors(HttpServletRequest request,
            HttpServletResponse response) {

String origin = request.getHeader("Origin");

if ("http://localhost:5501".equals(origin)
   || "http://127.0.0.1:5501".equals(origin)) {

response.setHeader("Access-Control-Allow-Origin", origin);
response.setHeader("Access-Control-Allow-Credentials", "true");
response.setHeader("Vary", "Origin");
}

response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
response.setHeader("Access-Control-Allow-Headers", "Content-Type");
}
    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
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

    
    // SUBMIT FEEDBACK
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

        String action = request.getParameter("feedbackAction");

        if (!"add".equals(action)) {
            sendMessage(response, "Invalid feedback action.");
            return;
        }

        String parkingValue = request.getParameter("parkingId");
        String ratingValue = request.getParameter("rating");
        String comments = request.getParameter("comments");

        if (parkingValue == null || ratingValue == null) {
            sendMessage(response, "Parking and rating are required.");
            return;
        }

        try {

            int parkingId = Integer.parseInt(parkingValue);
            int rating = Integer.parseInt(ratingValue);

            if (parkingId <= 0 || rating < 1 || rating > 5) {
                sendMessage(response, "Enter a valid parking ID and rating from 1 to 5.");
                return;
            }
         // Check whether parking exists in owner_db
            boolean parkingExists = false;

            String checkSql =
                "SELECT parking_id FROM parking WHERE parking_id = ?";

            try (Connection ownerCon = myconnect.myconnection();
                 PreparedStatement checkPs =
                     ownerCon.prepareStatement(checkSql)) {

                checkPs.setInt(1, parkingId);

                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        parkingExists = true;
                    }
                }
            }

            if (!parkingExists) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid parking location.");
                return;
            }

            String sql = "INSERT INTO feedback "
                       + "(user_id, parking_id, rating, comments) "
                       + "VALUES (?, ?, ?, ?)";

            try (Connection con = mycon.myownconnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, parkingId);
                ps.setInt(3, rating);
                ps.setString(4, comments);

                int result = ps.executeUpdate();

                if (result > 0) {
                    sendMessage(response, "Feedback submitted successfully.");
                } else {
                    sendMessage(response, "Unable to submit feedback.");
                }
            }

        } catch (NumberFormatException e) {

            sendMessage(response, "Invalid parking ID or rating.");

        } catch (Exception e) {

            e.printStackTrace();
            sendMessage(response, "Database error while submitting feedback.");
        }
    }

    // OWNER VIEWS FEEDBACK
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            sendMessage(response, "Please login first.");
            return;
        }

        int ownerId = (Integer) session.getAttribute("userId");

        String action = request.getParameter("feedbackAction");

        if (!"viewOwner".equals(action)) {
            sendMessage(response, "Invalid feedback action.");
            return;
        }

        String sql = "SELECT f.feedback_id, f.user_id, f.parking_id, "
                   + "f.rating, f.comments, f.feedback_date "
                   + "FROM feedback f "
                   + "JOIN parking p ON f.parking_id = p.parking_id "
                   + "WHERE p.owner_id = ? "
                   + "ORDER BY f.feedback_date DESC";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {

                java.io.PrintWriter out = response.getWriter();

                out.println("<html><head><title>Owner Feedback</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;padding:20px;}");
                out.println("table{border-collapse:collapse;width:100%;}");
                out.println("th,td{border:1px solid #ccc;padding:10px;text-align:center;}");
                out.println("th{background:#333;color:white;}");
                out.println("</style></head><body>");

                out.println("<h2>Customer Feedback</h2>");

                out.println("<table>");
                out.println("<tr>");
                out.println("<th>Feedback ID</th>");
                out.println("<th>User ID</th>");
                out.println("<th>Parking ID</th>");
                out.println("<th>Rating</th>");
                out.println("<th>Comments</th>");
                out.println("<th>Date</th>");
                out.println("</tr>");

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    out.println("<tr>");

                    out.println("<td>" + rs.getInt("feedback_id") + "</td>");
                    out.println("<td>" + rs.getInt("user_id") + "</td>");
                    out.println("<td>" + rs.getInt("parking_id") + "</td>");
                    out.println("<td>" + rs.getInt("rating") + "</td>");

                    out.println("<td>"
                            + escapeHTML(rs.getString("comments"))
                            + "</td>");

                    out.println("<td>"
                            + rs.getTimestamp("feedback_date")
                            + "</td>");

                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='6'>No feedback available.</td></tr>");
                }

                out.println("</table>");
                out.println("</body></html>");
            }

        } catch (Exception e) {

            e.printStackTrace();
            sendMessage(response, "Unable to load feedback.");
        }
    }
}
