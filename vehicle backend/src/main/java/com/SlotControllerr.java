
package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.SlotData;
import com.model.service.UserServicee;

public class SlotControllerr extends HttpServlet {

    private static final long serialVersionUID = 1L;

    UserServicee service = new UserServicee();

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

        setCors(request, response);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {

            String action = request.getParameter("slotAction");

            if (!"view".equals(action)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid slot action\"}");
                return;
            }

            // Check logged-in user session
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"message\":\"Please login first\"}");
                return;
            }

            String role = String.valueOf(session.getAttribute("userRole"));

            if (!"USER".equalsIgnoreCase(role)
                    && !"OWNER".equalsIgnoreCase(role)) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\":false,\"message\":\"Access denied\"}");
                return;
            }

            String parkingIdValue = request.getParameter("parkingId");
            String vehicleType = request.getParameter("vehicleType");

            if (parkingIdValue == null || vehicleType == null
                    || parkingIdValue.trim().isEmpty()
                    || vehicleType.trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Parking ID and vehicle type are required\"}");
                return;
            }

            int parkingId = Integer.parseInt(parkingIdValue);

            vehicleType = vehicleType.trim().toUpperCase();

            if (!"CAR".equals(vehicleType)
                    && !"BIKE".equals(vehicleType)) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid vehicle type\"}");
                return;
            }

            // Fetch slots from owner_db
            List<SlotData> slotList =
                    service.getPublicParkingSlots(parkingId, vehicleType);

            out.print("{\"success\":true,\"slots\":[");

            if (slotList != null) {

                for (int i = 0; i < slotList.size(); i++) {

                    SlotData slot = slotList.get(i);

                    if (i > 0) {
                        out.print(",");
                    }

                    out.print("{");
                    out.print("\"slotId\":" + slot.getSlotId() + ",");
                    out.print("\"parkingId\":" + slot.getParkingId() + ",");
                    out.print("\"slotNumber\":\"" +
                            escapeJson(slot.getSlotNumber()) + "\",");
                    out.print("\"vehicleType\":\"" +
                            escapeJson(slot.getVehicleType()) + "\",");
                    out.print("\"status\":\"" +
                            escapeJson(slot.getStatus()) + "\"");
                    out.print("}");
                }
            }

            out.print("]}");

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid parking ID\"}");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Unable to load parking slots\"}");
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
