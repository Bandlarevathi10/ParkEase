
package com;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.UserProfileData;
import com.model.service.UserProfileService;

public class UserProfileController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    UserProfileService service = new UserProfileService();

    // CORS configuration
    private void setCors(HttpServletRequest req, HttpServletResponse res) {

        String origin = req.getHeader("Origin");

        if ("http://localhost:5501".equals(origin)
                || "http://127.0.0.1:5501".equals(origin)) {

            res.setHeader("Access-Control-Allow-Origin", origin);
        }

        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req,
                             HttpServletResponse res) {

        setCors(req, res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    // GET: Display logged-in user's profile
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws ServletException, IOException {

        setCors(req, res);

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        System.out.println("========== PROFILE REQUEST ==========");

        HttpSession session = req.getSession(false);

        System.out.println("Session exists: " + (session != null));

        if (session == null) {
            System.out.println("No session found");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write(
                "{\"success\":false,\"message\":\"Session not found. Please login again.\"}"
            );
            return;
        }

        Object id = session.getAttribute("userId");

        System.out.println("Profile User ID: " + id);

        if (id == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write(
                "{\"success\":false,\"message\":\"User ID not found in session\"}"
            );
            return;
        }

        int userId = ((Number) id).intValue();

        System.out.println("Fetching profile for user: " + userId);

        UserProfileData user = service.getUserProfile(userId);

        if (user == null) {
            System.out.println("Profile data not found. Check service/database.");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(
                "{\"success\":false,\"message\":\"Profile not found. Check database.\"}"
            );
            return;
        }

        System.out.println("Profile loaded: " + user.getName());

        String json = "{"
            + "\"success\":true,"
            + "\"name\":\"" + escape(user.getName()) + "\","
            + "\"email\":\"" + escape(user.getEmail()) + "\","
            + "\"phone\":\"" + escape(user.getPhone()) + "\","
            + "\"role\":\"" + escape(user.getRole()) + "\""
            + "}";

        res.getWriter().write(json);
    }  
    // POST: Update logged-in user's profile
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res)
            throws ServletException, IOException {

        setCors(req, res);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            res.getWriter().write(
                "{\"success\":false,\"message\":\"Please login first\"}"
            );

            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || phone == null || phone.trim().isEmpty()) {

            res.getWriter().write(
                "{\"success\":false,\"message\":\"All fields are required\"}"
            );

            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            res.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid email address\"}"
            );

            return;
        }

        if (!phone.matches("[0-9]{10}")) {

            res.getWriter().write(
                "{\"success\":false,\"message\":\"Enter a valid 10-digit phone number\"}"
            );

            return;
        }

        UserProfileData user = new UserProfileData();

        user.setUserId(userId);
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPhone(phone.trim());

        boolean result = service.updateUserProfile(user);

        if (result) {

            session.setAttribute("userName", name.trim());
            session.setAttribute("userEmail", email.trim());
            session.setAttribute("userPhone", phone.trim());

            res.getWriter().write(
                "{\"success\":true,\"message\":\"Profile updated successfully\"}"
            );

        } else {

            res.getWriter().write(
                "{\"success\":false,\"message\":\"Profile update failed. Check whether this email is already registered.\"}"
            );
        }
    }

    // Escape special characters before creating JSON response
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
