package com;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pureconnect.mycon;
public class Gateway extends HttpServlet {
 
    private void setCors(HttpServletRequest request,
                         HttpServletResponse response) {

        String origin = request.getHeader("Origin");
    
        if ("http://localhost:5501".equals(origin)
                || "http://127.0.0.1:5501".equals(origin)) {

            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Vary", "Origin");
        }
        response.setHeader(
            "Access-Control-Allow-Methods", "GET, POST, OPTIONS"
        );

        response.setHeader(
            "Access-Control-Allow-Headers", "Content-Type"
        );
    }

    // Handle browser CORS preflight
    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws ServletException, IOException {

        setCors(req, res);

        res.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "GET method is not allowed. Please use POST."
        );
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

        String action = request.getParameter("action");
        System.out.println("GATEWAY RECEIVED ACTION: " + action);
        System.out.println("COMPLAINT ACTION: " +
                request.getParameter("complaintAction"));
        System.out.println("FEEDBACK ACTION: " +
                request.getParameter("feedbackAction"));

        if ("Register".equals(action)) {
            request.getRequestDispatcher("/RegisterControllerr").forward(request,response);

        } else if ("Login".equals(action)) {
        	request.getRequestDispatcher("/LoginControllerr").forward(request, response);

        }  else if ("Payment".equals(action)) {
            request.getRequestDispatcher("/PaymentControllerr").forward(request, response);

        } else if ("Slots".equals(action)) {
            request.getRequestDispatcher("/SlotControllerr").forward(request, response);
        }else if ("Cancel".equals(action)) {
        	request.getRequestDispatcher("/CancelBookingControllerr").forward(request, response);

        } else if ("Booking".equals(action)) {
            request.getRequestDispatcher("/BookingControllerr").forward(request, response);

        } else if ("Complaint".equals(action)) {
        	request.getRequestDispatcher("/ComplaintControllerr").forward(request,response);

        } else if ("Feedback".equals(action)) {
        	request.getRequestDispatcher("/FeedbackControllerr").forward(request, response);

        } else if ("Parking".equals(action)) {
        	request.getRequestDispatcher("/ParkingControllerr").forward(request, response);

        } else if ("Search".equals(action)) {
        	request.getRequestDispatcher("/SearchParkingControllerr").forward(request, response);

        } 
        else if ("OwnerBoard".equals(action)) {
        	request.getRequestDispatcher("/OwnerDashboardController").forward(request, response);

        } else {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
        
    }
}

