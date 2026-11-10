package com;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.data.Userdataa;
import com.model.service.UserServicee;

public class LoginControllerr extends HttpServlet {
		UserServicee service =new UserServicee();
	protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
		 

	        request.setCharacterEncoding("UTF-8");

	        String email = request.getParameter("email");
	        String password = request.getParameter("password");

	        if (email == null || password == null ||
	                email.trim().isEmpty() || password.trim().isEmpty()) {

	            response.sendRedirect(
	                "http://localhost:5501/login.html?status=empty"
	            );
	            return;
	        }

	        email = email.trim();

	        Userdataa user = service.loginUser(email, password);

	        if (user != null) {

	            HttpSession session = request.getSession(true);

	            session.setAttribute("userId", user.getUserId());
	            session.setAttribute("userName", user.getFullName());
	            session.setAttribute("userEmail", user.getEmail());
	            session.setAttribute("userPhone", user.getPhone());
	            session.setAttribute("userRole", user.getRole());

	            session.setMaxInactiveInterval(30 * 60);

	            System.out.println("LOGIN SUCCESSFUL");
	            System.out.println("USER ID: " + user.getUserId());

	            if ("OWNER".equalsIgnoreCase(user.getRole())) {

	                response.sendRedirect(
	                    "http://localhost:5501/owner-dashboard.html"
	                );

	            } else if ("USER".equalsIgnoreCase(user.getRole())) {

	                response.sendRedirect(
	                    "http://localhost:5501/user-dashboard.html"
	                );

	            } else {

	                session.invalidate();

	                response.sendRedirect(
	                    "http://localhost:5501/login.html?status=invalidRole"
	                );
	            }

	        } else {

	            response.sendRedirect(
	                "http://localhost:5501/login.html?status=invalid"
	            );
	        }
	    }
}
