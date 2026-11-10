
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

public class OwnerComplaintController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private void setCors(HttpServletRequest request,
                         HttpServletResponse response) {

        String origin = request.getHeader("Origin");

        if ("http://localhost:5501".equals(origin) ||
            "http://127.0.0.1:5501".equals(origin)) {

            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) {

        setCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean checkOwner(HttpServletRequest request,
                               HttpServletResponse response)
                               throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null ||
            !"OWNER".equalsIgnoreCase(
                String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().print(
                "{\"success\":false,\"message\":\"Owner login required\"}"
            );

            return false;
        }

        return true;
    }

    private String json(String value) {

        if (value == null) {
            return "null";
        }

        return "\"" +
            value.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t")
            + "\"";
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        setCors(request, response);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!checkOwner(request, response)) {
            return;
        }

        String sql =
            "SELECT c.complaint_id, c.user_id, u.name, " +
            "c.booking_id, c.subject, c.description, " +
            "c.complaint_status, c.complaint_date, " +
            "COALESCE(vp.parking_name, op.parking_name, 'Not linked') " +
            "AS parking_name " +
            "FROM vehicle.complaints c " +
            "LEFT JOIN vehicle.users u ON c.user_id = u.user_id " +
            "LEFT JOIN vehicle.bookings b ON c.booking_id = b.booking_id " +
            "LEFT JOIN vehicle.parking vp " +
            "ON b.parking_source = 'VEHICLE_DB' " +
            "AND b.parking_id = vp.parking_id " +
            "LEFT JOIN owner_db.parking op " +
            "ON b.parking_source = 'OWNER_DB' " +
            "AND b.parking_id = op.parking_id " +
            "ORDER BY c.complaint_date DESC";

        try (
            Connection con = mycon.myownconnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            PrintWriter out = response.getWriter()
        ) {

            StringBuilder result = new StringBuilder();
            result.append("{\"success\":true,\"complaints\":[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    result.append(",");
                }

                first = false;

                result.append("{");

                result.append("\"complaintId\":")
                      .append(rs.getInt("complaint_id")).append(",");

                result.append("\"userId\":")
                      .append(rs.getInt("user_id")).append(",");

                result.append("\"customer\":")
                      .append(json(rs.getString("name"))).append(",");

                result.append("\"bookingId\":")
                      .append(rs.getObject("booking_id") == null
                          ? "null"
                          : rs.getInt("booking_id")).append(",");

                result.append("\"parkingName\":")
                      .append(json(rs.getString("parking_name"))).append(",");

                result.append("\"subject\":")
                      .append(json(rs.getString("subject"))).append(",");

                result.append("\"description\":")
                      .append(json(rs.getString("description"))).append(",");

                result.append("\"status\":")
                      .append(json(rs.getString("complaint_status"))).append(",");

                result.append("\"date\":")
                      .append(json(rs.getString("complaint_date")));

                result.append("}");
            }

            result.append("]}");

            out.print(result.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Unable to load complaints\"}"
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        setCors(request, response);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!checkOwner(request, response)) {
            return;
        }

        String action = request.getParameter("action");

        if (!"updateStatus".equals(action)) {

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Invalid action\"}"
            );

            return;
        }

        String complaintId = request.getParameter("complaintId");
        String status = request.getParameter("status");

        if (complaintId == null || status == null ||
            !status.matches("OPEN|IN PROGRESS|RESOLVED")) {

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Invalid complaint details\"}"
            );

            return;
        }

        String sql =
            "UPDATE vehicle.complaints " +
            "SET complaint_status = ? WHERE complaint_id = ?";

        try (
            Connection con = mycon.myownconnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(complaintId));

            int rows = ps.executeUpdate();

            if (rows > 0) {

                response.getWriter().print(
                    "{\"success\":true,\"message\":\"Complaint status updated\"}"
                );

            } else {

                response.getWriter().print(
                    "{\"success\":false,\"message\":\"Complaint not found\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Unable to update complaint\"}"
            );
        }
    }
}
