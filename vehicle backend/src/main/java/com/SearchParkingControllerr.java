
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

import com.pureconnect2.myconnect;

public class SearchParkingControllerr extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        String search = request.getParameter("search");

        String sql =
            "SELECT p.parking_id, p.parking_name, p.location, p.city, " +
            "p.address, p.total_slots, p.price_per_hour, p.description, " +

            "(SELECT COUNT(*) FROM slots s " +
            "WHERE s.parking_id = p.parking_id " +
            "AND UPPER(s.status) = 'AVAILABLE') AS available_slots, " +

            "(SELECT COUNT(*) FROM slots s " +
            "WHERE s.parking_id = p.parking_id " +
            "AND UPPER(s.vehicle_type) = 'CAR') AS car_slots, " +

            "(SELECT COUNT(*) FROM slots s " +
            "WHERE s.parking_id = p.parking_id " +
            "AND UPPER(s.vehicle_type) = 'BIKE') AS bike_slots " +

            "FROM parking p " +
            "WHERE UPPER(TRIM(p.status)) = 'ACTIVE' " +

            "AND (? IS NULL OR p.parking_name LIKE ? " +
            "OR p.city LIKE ? OR p.location LIKE ?) " +

            "ORDER BY p.parking_id DESC";

        try (Connection con = myconnect.myconnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (search == null || search.trim().isEmpty()) {

                ps.setNull(1, java.sql.Types.VARCHAR);
                ps.setNull(2, java.sql.Types.VARCHAR);
                ps.setNull(3, java.sql.Types.VARCHAR);
                ps.setNull(4, java.sql.Types.VARCHAR);

            } else {

                String value = "%" + search.trim() + "%";

                ps.setString(1, search.trim());
                ps.setString(2, value);
                ps.setString(3, value);
                ps.setString(4, value);
            }

            try (ResultSet rs = ps.executeQuery()) {

                out.print("{\"success\":true,\"parkings\":[");

                boolean first = true;

                while (rs.next()) {

                    if (!first) {
                        out.print(",");
                    }

                    first = false;

                    out.print("{");

                    out.print("\"parkingId\":" +
                            rs.getInt("parking_id") + ",");

                    out.print("\"parkingName\":\"" +
                            escapeJson(rs.getString("parking_name")) + "\",");

                    out.print("\"location\":\"" +
                            escapeJson(rs.getString("location")) + "\",");

                    out.print("\"city\":\"" +
                            escapeJson(rs.getString("city")) + "\",");

                    out.print("\"address\":\"" +
                            escapeJson(rs.getString("address")) + "\",");

                    out.print("\"totalSlots\":" +
                            rs.getInt("total_slots") + ",");

                    out.print("\"pricePerHour\":" +
                            rs.getDouble("price_per_hour") + ",");

                    out.print("\"description\":\"" +
                            escapeJson(rs.getString("description")) + "\",");

                    out.print("\"availableSlots\":" +
                            rs.getInt("available_slots") + ",");

                    out.print("\"carSlots\":" +
                            rs.getInt("car_slots") + ",");

                    out.print("\"bikeSlots\":" +
                            rs.getInt("bike_slots"));

                    out.print("}");
                }

                out.print("]}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                "{\"success\":false,\"message\":\"Unable to load parking locations\"}"
            );
        }
    }

    private String escapeJson(String value) {

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
