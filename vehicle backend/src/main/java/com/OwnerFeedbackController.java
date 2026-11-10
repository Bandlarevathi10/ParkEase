
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

public class OwnerFeedbackController extends HttpServlet {

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

        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) {

        setCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
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

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || !"OWNER".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please login as an owner.\"}"
            );
            return;
        }

        int ownerId = (Integer) session.getAttribute("userId");

        String sql =
            "SELECT f.feedback_id, f.user_id, u.name AS customer_name, " +
            "f.parking_id, p.parking_name, f.rating, f.comments, " +
            "f.feedback_date " +
            "FROM vehicle.feedback f " +
            "JOIN vehicle.users u ON f.user_id = u.user_id " +
            "JOIN owner_db.parking p ON f.parking_id = p.parking_id " +
            "WHERE p.owner_id = ? " +
            "ORDER BY f.feedback_date DESC";

        StringBuilder json = new StringBuilder();

        json.append("{\"success\":true,\"feedback\":[");

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

                    json.append("{")
                        .append("\"feedbackId\":")
                        .append(rs.getInt("feedback_id"))
                        .append(",\"userId\":")
                        .append(rs.getInt("user_id"))
                        .append(",\"customer\":\"")
                        .append(jsonEscape(rs.getString("customer_name")))
                        .append("\",\"parkingId\":")
                        .append(rs.getInt("parking_id"))
                        .append(",\"parkingName\":\"")
                        .append(jsonEscape(rs.getString("parking_name")))
                        .append("\",\"rating\":")
                        .append(rs.getInt("rating"))
                        .append(",\"comments\":\"")
                        .append(jsonEscape(rs.getString("comments")))
                        .append("\",\"date\":\"")
                        .append(jsonEscape(
                            String.valueOf(rs.getTimestamp("feedback_date"))
                        ))
                        .append("\"}");
                }
            }

            json.append("]}");
            response.getWriter().write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load feedback.\"}"
            );
        }
    }
}
