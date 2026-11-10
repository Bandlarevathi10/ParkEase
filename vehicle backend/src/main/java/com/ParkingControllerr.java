
package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.ParkingData;
import com.model.data.PaymentData;
import com.model.data.SlotData;
import com.model.service.UserServicee;

public class ParkingControllerr extends HttpServlet {

    private static final long serialVersionUID = 1L;

    UserServicee service = new UserServicee();
    //ParkingData p = new ParkingData();
    // CORS headers
    private void setCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin",
                "http://localhost:5501");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type");
    }

    // Handle preflight requests
    @Override
    protected void doOptions(HttpServletRequest request,
            HttpServletResponse response) {

        setCors(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Check whether parking belongs to logged-in owner
    private boolean isOwnerParking(int ownerId, int parkingId) {

        List<ParkingData> parkingList =
                service.getOwnerParking(ownerId);

        for (ParkingData parking : parkingList) {
            if (parking.getParkingId() == parkingId) {
                return true;
            }
        }

        return false;
    }

    // JSON response
    private void sendJson(HttpServletResponse response,
            boolean success, String message) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        out.print("{\"success\":" + success
                + ",\"message\":\"" + escapeJson(message) + "\"}");

        out.flush();
    }

    // Check owner session
    private Integer getOwnerId(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null ||
                !"OWNER".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            sendJson(response, false, "Owner login required.");
            return null;
        }

        return Integer.parseInt(
                session.getAttribute("userId").toString());
    }

    // Escape JSON strings
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

    // Validate parking status
    private boolean isValidParkingStatus(String status) {

        return status != null &&
                (status.equalsIgnoreCase("ACTIVE") ||
                 status.equalsIgnoreCase("INACTIVE"));
    }

    // ==========================================
    // GET REQUESTS
    // ==========================================

@Override
protected void doGet(HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    setCors(response);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    HttpSession session = request.getSession(false);

    if (session == null ||
            session.getAttribute("userId") == null) {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        sendJson(response, false, "Please login first.");
        return;
    }

    String role = String.valueOf(session.getAttribute("userRole"));

    String parkingAction = request.getParameter("parkingAction");
    String slotAction = request.getParameter("slotAction");

    try {

        // ======================================
        // CUSTOMER: VIEW ACTIVE PARKING LOCATIONS
        // ======================================

        if ("view".equalsIgnoreCase(parkingAction)
                && "USER".equalsIgnoreCase(role)) {

            List<ParkingData> parkings =
                    service.getAllActiveParkings();

            PrintWriter out = response.getWriter();

            out.print("{\"success\":true,\"parkings\":[");

            for (int i = 0; i < parkings.size(); i++) {

                ParkingData p = parkings.get(i);

                if (i > 0) out.print(",");

                out.print("{");
                out.print("\"parkingId\":" + p.getParkingId() + ",");
                out.print("\"parkingName\":\"" + escapeJson(p.getParkingName()) + "\",");
                out.print("\"city\":\"" + escapeJson(p.getCity()) + "\",");
                out.print("\"location\":\"" + escapeJson(p.getLocation()) + "\",");
                out.print("\"address\":\"" + escapeJson(p.getAddress()) + "\",");
                out.print("\"totalSlots\":" + p.getTotalSlots() + ",");
                out.print("\"pricePerHour\":" + p.getPricePerHour() + ",");
                out.print("\"description\":\"" + escapeJson(p.getDescription()) + "\"");
                out.print("}");
            }

            out.print("]}");
            return;
        }

        // ======================================
        // OWNER: VIEW OWN PARKING LOCATIONS
        // ======================================

        if ("view".equalsIgnoreCase(parkingAction)
                && "OWNER".equalsIgnoreCase(role)) {

            int ownerId = Integer.parseInt(
                    session.getAttribute("userId").toString());

            List<ParkingData> parkings =
                    service.getOwnerParking(ownerId);

            PrintWriter out = response.getWriter();

            out.print("{\"success\":true,\"parkings\":[");

            for (int i = 0; i < parkings.size(); i++) {

                ParkingData p = parkings.get(i);

                if (i > 0) out.print(",");

                out.print("{");
                out.print("\"parkingId\":" + p.getParkingId() + ",");
                out.print("\"parkingName\":\"" + escapeJson(p.getParkingName()) + "\",");
                out.print("\"city\":\"" + escapeJson(p.getCity()) + "\",");
                out.print("\"location\":\"" + escapeJson(p.getLocation()) + "\",");
                out.print("\"address\":\"" + escapeJson(p.getAddress()) + "\",");
                out.print("\"totalSlots\":" + p.getTotalSlots() + ",");
                out.print("\"pricePerHour\":" + p.getPricePerHour() + ",");
                out.print("\"status\":\"" + escapeJson(p.getStatus()) + "\",");
                out.print("\"description\":\"" + escapeJson(p.getDescription()) + "\"");
                out.print("}");
            }

            out.print("]}");
            return;
        }

        // ======================================
        // CUSTOMER: VIEW SLOTS
        // ======================================

        if ("view".equalsIgnoreCase(slotAction)
                && "USER".equalsIgnoreCase(role)) {

            int parkingId = Integer.parseInt(
                    request.getParameter("parkingId"));

            String vehicleType =
                    request.getParameter("vehicleType");

            if (!isValidVehicleType(vehicleType)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJson(response, false, "Select CAR or BIKE.");
                return;
            }

            List<SlotData> slots =
                    service.getPublicParkingSlots(parkingId, vehicleType);

            PrintWriter out = response.getWriter();

            out.print("{\"success\":true,\"slots\":[");

            for (int i = 0; i < slots.size(); i++) {

                SlotData s = slots.get(i);

                if (i > 0) out.print(",");

                out.print("{");
                out.print("\"slotId\":" + s.getSlotId() + ",");
                out.print("\"parkingId\":" + s.getParkingId() + ",");
                out.print("\"slotNumber\":\"" + escapeJson(s.getSlotNumber()) + "\",");
                out.print("\"vehicleType\":\"" + escapeJson(s.getVehicleType()) + "\",");
                out.print("\"status\":\"" + escapeJson(s.getStatus()) + "\"");
                out.print("}");
            }

            out.print("]}");
            return;
        }

        // ======================================
        // OWNER: VIEW OWN PARKING SLOTS
        // ======================================

        if ("view".equalsIgnoreCase(slotAction)
                && "OWNER".equalsIgnoreCase(role)) {

            int ownerId = Integer.parseInt(
                    session.getAttribute("userId").toString());

            int parkingId = Integer.parseInt(
                    request.getParameter("parkingId"));

            if (!isOwnerParking(ownerId, parkingId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                sendJson(response, false, "You cannot access this parking.");
                return;
            }

            String vehicleType = request.getParameter("vehicleType");

            List<SlotData> slots =
                    service.getParkingSlots(parkingId, vehicleType);

            PrintWriter out = response.getWriter();

            out.print("{\"success\":true,\"slots\":[");

            for (int i = 0; i < slots.size(); i++) {

                SlotData s = slots.get(i);

                if (i > 0) out.print(",");

                out.print("{");
                out.print("\"slotId\":" + s.getSlotId() + ",");
                out.print("\"parkingId\":" + s.getParkingId() + ",");
                out.print("\"slotNumber\":\"" + escapeJson(s.getSlotNumber()) + "\",");
                out.print("\"vehicleType\":\"" + escapeJson(s.getVehicleType()) + "\",");
                out.print("\"status\":\"" + escapeJson(s.getStatus()) + "\"");
                out.print("}");
            }

            out.print("]}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        sendJson(response, false, "Invalid action.");

    } catch (NumberFormatException e) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        sendJson(response, false, "Invalid ID.");

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        sendJson(response, false, "Unable to load requested details.");
    }
}


    // ==========================================
    // POST REQUESTS
    // ==========================================

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setCors(response);

        Integer ownerId = getOwnerId(request, response);

        if (ownerId == null) {
            return;
        }

        String parkingAction =
                request.getParameter("parkingAction");

        String slotAction =
                request.getParameter("slotAction");
        //int carSlots = Integer.parseInt(
                //request.getParameter("carSlots"));

       // int bikeSlots = Integer.parseInt(
                //request.getParameter("bikeSlots"));
        try {

            // ==================================
            // ADD PARKING
            // ==================================
        
        	if ("add".equalsIgnoreCase(parkingAction)) {

        	    String carValue = request.getParameter("carSlots");
        	    String bikeValue = request.getParameter("bikeSlots");

        	    if (carValue == null || carValue.trim().isEmpty() ||
        	            bikeValue == null || bikeValue.trim().isEmpty()) {

        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        sendJson(response, false,
        	                "Please enter both car and bike slot counts.");
        	        return;
        	    }

        	    int carSlots = Integer.parseInt(carValue);
        	    int bikeSlots = Integer.parseInt(bikeValue);

        	    if (carSlots < 0 || bikeSlots < 0 ||
        	            carSlots + bikeSlots == 0) {

        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        sendJson(response, false,
        	                "Enter valid car and bike slot counts.");
        	        return;
        	    }

        	    ParkingData parking = getParkingData(request);

        	    parking.setOwnerId(ownerId);
        	    parking.setTotalSlots(carSlots + bikeSlots);

        	    if (!isValidParking(parking)) {

        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        sendJson(response, false,
        	                "Please enter valid parking details.");
        	        return;
        	    }

        	    boolean success = service.addParkingWithSlots(
        	            parking, ownerId, carSlots, bikeSlots);

        	    if (success) {
        	        sendJson(response, true,
        	                "Parking location and slots added successfully.");
        	    } else {
        	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	        sendJson(response, false,
        	                "Unable to add parking location.");
        	    }
        	}
            // ==================================
            // UPDATE PARKING
            // ==================================

            else if ("update".equalsIgnoreCase(parkingAction)) {

                int parkingId = Integer.parseInt(
                        request.getParameter("parkingId"));

                if (!isOwnerParking(ownerId, parkingId)) {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN);

                    sendJson(response, false,
                            "You cannot update this parking.");

                    return;
                }

                ParkingData parking = getParkingData(request);

                parking.setParkingId(parkingId);
                parking.setOwnerId(ownerId);

                if (!isValidParking(parking)) {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    sendJson(response, false,
                            "Please enter valid parking details.");

                    return;
                }

                boolean result = service.updateParking(parking);

                if (result) {

                    sendJson(response, true,
                            "Parking location updated successfully.");

                } else {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    sendJson(response, false,
                            "Unable to update parking location.");
                }
            }

            // ==================================
            // DELETE PARKING
            // ==================================

            else if ("delete".equalsIgnoreCase(parkingAction)) {

                int parkingId = Integer.parseInt(
                        request.getParameter("parkingId"));

                if (!isOwnerParking(ownerId, parkingId)) {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN);

                    sendJson(response, false,
                            "You cannot delete this parking.");

                    return;
                }

                boolean result =
                        service.deleteParking(parkingId, ownerId);

                if (result) {

                    sendJson(response, true,
                            "Parking location deleted successfully.");

                } else {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    sendJson(response, false,
                            "Unable to delete parking. Check whether slots or bookings are linked to it.");
                }
            }

            // ==================================
            // ADD SLOT
            // ==================================

            else if ("add".equalsIgnoreCase(slotAction)) {

                int parkingId = Integer.parseInt(
                        request.getParameter("parkingId"));

                if (!isOwnerParking(ownerId, parkingId)) {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN);

                    sendJson(response, false,
                            "You cannot add slots to this parking.");

                    return;
                }

                String slotNumber =
                        request.getParameter("slotNumber");

                String vehicleType =
                        request.getParameter("vehicleType");

                if (slotNumber == null ||
                        slotNumber.trim().isEmpty() ||
                        !isValidVehicleType(vehicleType)) {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    sendJson(response, false,
                            "Enter a valid slot number and vehicle type.");

                    return;
                }

                SlotData slot = new SlotData();

                slot.setParkingId(parkingId);
                slot.setSlotNumber(slotNumber.trim());
                slot.setVehicleType(vehicleType.toUpperCase());
                slot.setStatus("AVAILABLE");

                boolean result = service.addSlot(slot);

                sendJson(response, result,
                        result ? "Slot added successfully."
                               : "Unable to add slot.");
            }

            // ==================================
            // UPDATE SLOT
            // ==================================

            else if ("update".equalsIgnoreCase(slotAction)) {

                int slotId = Integer.parseInt(
                        request.getParameter("slotId"));

                int parkingId = Integer.parseInt(
                        request.getParameter("parkingId"));

                if (!isOwnerParking(ownerId, parkingId)) {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN);

                    sendJson(response, false,
                            "You cannot update this parking.");

                    return;
                }

                String slotNumber =
                        request.getParameter("slotNumber");

                String vehicleType =
                        request.getParameter("vehicleType");

                String status =
                        request.getParameter("status");

                if (slotNumber == null ||
                        slotNumber.trim().isEmpty() ||
                        !isValidVehicleType(vehicleType) ||
                        !isValidStatus(status)) {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    sendJson(response, false,
                            "Invalid slot details.");

                    return;
                }

                SlotData slot = new SlotData();

                slot.setSlotId(slotId);
                slot.setParkingId(parkingId);
                slot.setSlotNumber(slotNumber.trim());
                slot.setVehicleType(vehicleType.toUpperCase());
                slot.setStatus(status.toUpperCase());

                boolean result = service.updateSlot(slot);

                sendJson(response, result,
                        result ? "Slot updated successfully."
                               : "Unable to update slot.");
            }

            // ==================================
            // DELETE SLOT
            // ==================================

            else if ("delete".equalsIgnoreCase(slotAction)) {

                int slotId = Integer.parseInt(
                        request.getParameter("slotId"));

                int parkingId = Integer.parseInt(
                        request.getParameter("parkingId"));

                if (!isOwnerParking(ownerId, parkingId)) {

                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN);

                    sendJson(response, false,
                            "You cannot delete this parking slot.");

                    return;
                }

                boolean result =
                        service.deleteSlot(slotId, parkingId);

                sendJson(response, result,
                        result ? "Slot deleted successfully."
                               : "Unable to delete slot. Booked slots cannot be deleted.");
            }

            else {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                sendJson(response, false,
                        "Invalid parking or slot action.");
            }

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            sendJson(response, false, "Invalid ID or number.");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            sendJson(response, false,
                    "Server error while processing request.");
        }
    }

    private ParkingData getParkingData(HttpServletRequest request) {

        ParkingData parking = new ParkingData();

        parking.setParkingName(request.getParameter("parkingName"));
        parking.setCity(request.getParameter("city"));
        parking.setLocation(request.getParameter("location"));
        parking.setAddress(request.getParameter("address"));

        parking.setTotalSlots(Integer.parseInt(
                request.getParameter("totalSlots")));

        parking.setPricePerHour(Double.parseDouble(
                request.getParameter("pricePerHour")));

        parking.setStatus(request.getParameter("status"));
        parking.setDescription(request.getParameter("description"));

        return parking;
    }
    // Validate parking form values
    private boolean isValidParking(ParkingData p) {

        return p.getParkingName() != null &&
                p.getParkingName().trim().length() >= 3 &&
                p.getCity() != null &&
                p.getCity().trim().length() >= 2 &&
                p.getLocation() != null &&
                p.getLocation().trim().length() >= 2 &&
                p.getTotalSlots() > 0 &&
                p.getPricePerHour() > 0 &&
                isValidParkingStatus(p.getStatus());
    }

    private boolean isValidVehicleType(String type) {

        return type != null &&
                (type.equalsIgnoreCase("CAR") ||
                 type.equalsIgnoreCase("BIKE"));
    }

    private boolean isValidStatus(String status) {

        return status != null &&
                (status.equalsIgnoreCase("AVAILABLE") ||
                 status.equalsIgnoreCase("BOOKED"));
    }
}

