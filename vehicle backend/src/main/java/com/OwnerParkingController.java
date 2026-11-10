package com;

import java.io.IOException;
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

public class OwnerParkingController extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private void setCors(HttpServletRequest request,
                         HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        if ("http://localhost:5501".equals(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
    }

    private Integer getOwnerId(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null ||
            !"OWNER".equalsIgnoreCase(
                String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Please login as an owner.");
            return null;
        }

        return (Integer) session.getAttribute("userId");
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) {
        setCors(request, response);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    // Display parking locations belonging to the logged-in owner
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setContentType("text/html;charset=UTF-8");

        Integer ownerId = getOwnerId(request, response);

        if (ownerId == null) {
            return;
        }

        StringBuilder html = new StringBuilder();

        String sql =
            "SELECT parking_id, parking_name, location, city, " +
            "total_slots, price_per_hour, status " +
            "FROM parking WHERE owner_id = ? " +
            "ORDER BY parking_id DESC";

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    html.append("<tr>");

                    html.append("<td>")
                        .append(rs.getInt("parking_id"))
                        .append("</td>");

                    html.append("<td>")
                        .append(escape(rs.getString("parking_name")))
                        .append("</td>");

                    html.append("<td>")
                        .append(escape(rs.getString("location")))
                        .append("</td>");

                    html.append("<td>")
                        .append(escape(rs.getString("city")))
                        .append("</td>");

                    html.append("<td>")
                        .append(rs.getInt("total_slots"))
                        .append("</td>");

                    html.append("<td>₹")
                        .append(rs.getBigDecimal("price_per_hour"))
                        .append("</td>");

                    html.append("<td>")
                        .append(escape(rs.getString("status")))
                        .append("</td>");

                    html.append("</tr>");
                }
            }

            if (html.length() == 0) {
                html.append("<tr><td colspan='7'>")
                    .append("No parking locations added yet.")
                    .append("</td></tr>");
            }

            response.getWriter().print(html.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("Unable to load parking locations.");
        }
    }

    // Add a new parking location for the logged-in owner
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        Integer ownerId = getOwnerId(request, response);

        if (ownerId == null) {
            return;
        }

        String parkingName = request.getParameter("parking_name");
        String location = request.getParameter("location");
        String city = request.getParameter("city");
        String address = request.getParameter("address");
        String slotsText = request.getParameter("total_slots");
        String priceText = request.getParameter("price_per_hour");
        String description = request.getParameter("description");

        if (parkingName == null || location == null ||
            city == null || slotsText == null || priceText == null ||
            parkingName.trim().isEmpty() ||
            location.trim().isEmpty() ||
            city.trim().isEmpty() ||
            slotsText.trim().isEmpty() ||
            priceText.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("Please fill all required fields.");
            return;
        }

        int totalSlots;
        double price;

        try {
            totalSlots = Integer.parseInt(slotsText);
            price = Double.parseDouble(priceText);

            if (totalSlots <= 0 || price <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(
                "Enter a valid number of slots and price."
            );
            return;
        }

        String sql =
            "INSERT INTO parking " +
            "(owner_id, parking_name, location, city, address, " +
            "total_slots, price_per_hour, description) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            ps.setString(2, parkingName.trim());
            ps.setString(3, location.trim());
            ps.setString(4, city.trim());
            ps.setString(5, address);
            ps.setInt(6, totalSlots);
            ps.setDouble(7, price);
            ps.setString(8, description);

            int result = ps.executeUpdate();

            if (result > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().print(
                    "Parking location added successfully."
                );
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("Parking location was not added.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("Unable to add parking location.");
        }
    }

    private String escape(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
