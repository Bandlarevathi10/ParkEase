package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.data.Userdataa;
import com.model.service.UserServicee;
import com.pureconnect.mycon;

public class RegisterControllerr extends HttpServlet {
	
		UserServicee service=new UserServicee();
	   
	    protected void doPost(HttpServletRequest request,
	            HttpServletResponse response)
	            throws ServletException, IOException {

	        response.setContentType("text/html");

	        String fullname = request.getParameter("fullname");
	        String email = request.getParameter("email");
	        String phone = request.getParameter("phone");
	        String role = request.getParameter("role");
	        String password = request.getParameter("password");
	        String confirmPassword = request.getParameter("confirmPassword");

	        System.out.println("===== REGISTRATION STARTED =====");
	        System.out.println("Name: " + fullname);
	        System.out.println("Email: " + email);
	        System.out.println("Phone: " + phone);
	        System.out.println("Role: " + role);
	        // Check empty values
	        if (fullname == null || email == null || phone == null ||
	            role == null || password == null || confirmPassword == null ||
	            fullname.trim().isEmpty() || email.trim().isEmpty() ||
	            phone.trim().isEmpty() || role.trim().isEmpty() ||
	            password.isEmpty() || confirmPassword.isEmpty()) {

	            response.sendRedirect(request.getContextPath()
	                    + "/register.html?status=empty");
	            return;
	        }

	        // Check password match
	        if (!password.equals(confirmPassword)) {
	            response.sendRedirect(request.getContextPath()
	                    + "/register.html?status=passwordMismatch");
	            return;
	        }

	        // Check valid role
	        if (!"USER".equalsIgnoreCase(role) &&
	            !"OWNER".equalsIgnoreCase(role)) {

	            response.sendRedirect(request.getContextPath()
	                    + "/register.html?status=invalidRole");
	            return;
	        }

	        try {
	            Connection con = mycon.myownconnection();
	            PreparedStatement pst = con.prepareStatement( "INSERT INTO users(name,email,password,phone,role) "+ "VALUES(?,?,?,?,?)");

	            pst.setString(1, fullname);
	            pst.setString(2, email);
	            pst.setString(3, password);
	            pst.setString(4, phone);
	            pst.setString(5, role);

	            int result = pst.executeUpdate();

	            if (result > 0) {
                    // Account created successfully
	            	 response.sendRedirect("http://localhost:5501/login.html");
                } else {
                    response.sendRedirect(request.getContextPath()
                            + "/register.html?status=failed");
                }

	            pst.close();
	            con.close();

	        } catch (Exception e) {
	            e.printStackTrace();

	            response.setContentType("text/html");
	            response.getWriter().println(
	                "<h2>Registration Failed</h2>"
	            );
	            response.getWriter().println(
	                "<pre>" + e.getMessage() + "</pre>"
	            );
	        }
	    }
}


