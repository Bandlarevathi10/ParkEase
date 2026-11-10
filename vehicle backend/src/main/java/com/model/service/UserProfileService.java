
package com.model.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.pureconnect.mycon;
import com.model.data.UserProfileData;

public class UserProfileService {

    // Fetch user profile
    public UserProfileData getUserProfile(int userId) {

        UserProfileData user = null;

        String query = "SELECT user_id, name, email, phone, role "
                     + "FROM users WHERE user_id = ?";

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                user = new UserProfileData();

                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // Update user profile
    public boolean updateUserProfile(UserProfileData user) {

        String query = "UPDATE users SET name=?, email=?, phone=? "
                     + "WHERE user_id=?";

        try (Connection con = mycon.myownconnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setInt(4, user.getUserId());

            int result = ps.executeUpdate();

            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
