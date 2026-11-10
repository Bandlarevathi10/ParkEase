package com;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.pureconnect2.myconnect;

public class OwnerProfileController extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCors(request, response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || !"OWNER".equalsIgnoreCase(
                    String.valueOf(session.getAttribute("userRole")))) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Please login as an owner.\"}");
            return;
        }

        int ownerId;

        try {
            ownerId = Integer.parseInt(
                String.valueOf(session.getAttribute("userId"))
            );
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Invalid owner session.\"}");
            return;
        }

        String name = "";
        String email = "";
        String phone = "";
        String role = "";
        String businessName = "";
        String businessAddress = "";
        String contactPerson = "";
        String contactPhone = "";

        try {
            // Personal details from vehicle database
            try (Connection con = mycon.myownconnection();
                 PreparedStatement ps = con.prepareStatement(
                    "SELECT name, email, phone, role FROM users WHERE user_id = ?")) {

                ps.setInt(1, ownerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        name = rs.getString("name");
                        email = rs.getString("email");
                        phone = rs.getString("phone");
                        role = rs.getString("role");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Owner account not found.\"}");
                        return;
                    }
                }
            }

            // Business details from owner_db
            try (Connection con = myconnect.myconnection();
                 PreparedStatement ps = con.prepareStatement(
                    "SELECT business_name, business_address, "
                    + "contact_person, contact_phone "
                    + "FROM owner_profiles WHERE owner_id = ?")) {

                ps.setInt(1, ownerId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        businessName = rs.getString("business_name");
                        businessAddress = rs.getString("business_address");
                        contactPerson = rs.getString("contact_person");
                        contactPhone = rs.getString("contact_phone");
                    }
                }
            }

            out.print("{"
                + "\"name\":\"" + json(name) + "\","
                + "\"email\":\"" + json(email) + "\","
                + "\"phone\":\"" + json(phone) + "\","
                + "\"role\":\"" + json(role) + "\","
                + "\"businessName\":\"" + json(businessName) + "\","
                + "\"businessAddress\":\"" + json(businessAddress) + "\","
                + "\"contactPerson\":\"" + json(contactPerson) + "\","
                + "\"contactPhone\":\"" + json(contactPhone) + "\""
                + "}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Unable to load owner profile.\"}");
        }
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}