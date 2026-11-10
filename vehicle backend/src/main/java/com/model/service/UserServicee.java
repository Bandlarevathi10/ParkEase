package com.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import com.model.data.BookingData;
import com.model.data.FeedBackData;
import com.model.data.ParkingData;
import com.model.data.PaymentData;
import com.model.data.SlotData;
import com.model.data.Userdataa;
import com.model.data.VehicleData;
import com.model.data.ComplaintData;
import com.model.data.FeedBackData;
import com.model.design.Userdesign;

import com.pureconnect.mycon;
import com.pureconnect2.myconnect;

public class UserServicee implements Userdesign{
	

	public boolean registerUser(Userdataa d) {
        int r = 0;
        try {
            Connection con = mycon.myownconnection();
            PreparedStatement pst = con.prepareStatement("INSERT INTO users(name,email,password,phone,role) VALUES(?,?,?,?,?)");
            pst.setString(1, d.getFullName());
            pst.setString(2, d.getEmail());
            pst.setString(3, d.getPassword()); // d.getPassword() ni getPass() chesa format kosam
            pst.setString(4, d.getPhone()); // d.getPhone() ni getCont() chesa
            pst.setString(5, d.getRole());

            r = pst.executeUpdate();
            System.out.println("Rows inserted: " + r);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

	@Override
	public Userdataa loginUser(String email, String password) {

	    Userdataa ud = null;

	    try {
	    	
	    	Connection con = mycon.myownconnection();
	         PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE email=? AND password=?"); 

	        pst.setString(1, email);
	        pst.setString(2, password);

	        ResultSet rs = pst.executeQuery();

	        if (rs.next()) {

	            ud = new Userdataa();
	            ud.setUserId(rs.getInt("user_id"));
	            ud.setFullName(rs.getString("name"));
	            ud.setEmail(rs.getString("email"));
	            ud.setPassword(rs.getString("password"));
	            ud.setPhone(rs.getString("phone"));
	            ud.setRole(rs.getString("role"));
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return ud;
	}


	public Userdataa getUserProfile(int userId) {
		 Userdataa user = null;

	        String sql = "SELECT * FROM users WHERE user_id=?";

	        try (Connection con = mycon.myownconnection();
	             PreparedStatement pst = con.prepareStatement(sql)) {

	            pst.setInt(1, userId);

	            try (ResultSet rs = pst.executeQuery()) {

	                if (rs.next()) {
	                    user = new Userdataa();

	                    user.setUserId(rs.getInt("user_id"));
	                    user.setFullName(rs.getString("name"));
	                    user.setEmail(rs.getString("email"));
	                    user.setPassword(rs.getString("password"));
	                    user.setPhone(rs.getString("phone"));
	                    user.setRole(rs.getString("role"));
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return user;
	}

	public boolean updateUserProfile(Userdataa user) {
		String sql = "UPDATE users SET name=?, email=?, password=?, phone=? WHERE user_id=?";

        try (Connection con = mycon.myownconnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, user.getFullName());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPassword());
            pst.setString(4, user.getPhone());
            pst.setInt(5, user.getUserId());

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}

	public boolean addParking(ParkingData p) {

	    String sql = "INSERT INTO parking "
	            + "(owner_id, parking_name, location, city, address, "
	            + "total_slots, price_per_hour, description, status) "
	            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setInt(1, p.getOwnerId());
	        ps.setString(2, p.getParkingName());
	        ps.setString(3, p.getLocation());
	        ps.setString(4, p.getCity());
	        ps.setString(5, p.getAddress());
	        ps.setInt(6, p.getTotalSlots());
	        ps.setDouble(7, p.getPricePerHour());
	        ps.setString(8, p.getDescription());
	        ps.setString(9, p.getStatus());

	        return ps.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}


	public boolean updateParking(ParkingData p) {

	    String sql = "UPDATE parking SET parking_name=?, location=?, "
	            + "city=?, address=?, total_slots=?, price_per_hour=?, "
	            + "description=?, status=? "
	            + "WHERE parking_id=? AND owner_id=?";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setString(1, p.getParkingName());
	        ps.setString(2, p.getLocation());
	        ps.setString(3, p.getCity());
	        ps.setString(4, p.getAddress());
	        ps.setInt(5, p.getTotalSlots());
	        ps.setDouble(6, p.getPricePerHour());
	        ps.setString(7, p.getDescription());
	        ps.setString(8, p.getStatus());
	        ps.setInt(9, p.getParkingId());
	        ps.setInt(10, p.getOwnerId());

	        return ps.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean deleteParking(int parkingId, int ownerId) {

	    String sql = "DELETE FROM parking "
	            + "WHERE parking_id=? AND owner_id=?";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setInt(1, parkingId);
	        ps.setInt(2, ownerId);

	        return ps.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	public boolean addParkingWithSlots(ParkingData p, int ownerId,
            int carSlots, int bikeSlots) {

String parkingSql = "INSERT INTO parking "
+ "(owner_id, parking_name, location, city, address, "
+ "total_slots, price_per_hour, description, status) "
+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

String slotSql = "INSERT INTO slots "
+ "(parking_id, slot_number, vehicle_type, status) "
+ "VALUES (?, ?, ?, ?)";

int totalSlots = carSlots + bikeSlots;

try (Connection con = myconnect.myconnection()) {

con.setAutoCommit(false);

try {
int parkingId;

// 1. Insert parking location
try (PreparedStatement ps = con.prepareStatement(
parkingSql, Statement.RETURN_GENERATED_KEYS)) {

ps.setInt(1, ownerId);
ps.setString(2, p.getParkingName());
ps.setString(3, p.getLocation());
ps.setString(4, p.getCity());
ps.setString(5, p.getAddress());
ps.setInt(6, totalSlots);
ps.setDouble(7, p.getPricePerHour());
ps.setString(8, p.getDescription());
ps.setString(9, p.getStatus());

ps.executeUpdate();

try (ResultSet rs = ps.getGeneratedKeys()) {
if (!rs.next()) {
 throw new SQLException("Parking ID was not generated.");
}
parkingId = rs.getInt(1);
}
}

// 2. Generate CAR and BIKE slots
try (PreparedStatement ps = con.prepareStatement(slotSql)) {

for (int i = 1; i <= carSlots; i++) {
ps.setInt(1, parkingId);
ps.setString(2, String.format("C%02d", i));
ps.setString(3, "CAR");
ps.setString(4, "AVAILABLE");
ps.addBatch();
}

for (int i = 1; i <= bikeSlots; i++) {
ps.setInt(1, parkingId);
ps.setString(2, String.format("B%02d", i));
ps.setString(3, "BIKE");
ps.setString(4, "AVAILABLE");
ps.addBatch();
}

ps.executeBatch();
}

// 3. Save everything together
con.commit();
return true;

} catch (Exception e) {
con.rollback();
e.printStackTrace();
return false;
}

} catch (Exception e) {
e.printStackTrace();
return false;
}
}
	public List<ParkingData> getOwnerParking(int ownerId) {
		List<ParkingData> list = new ArrayList<>();

	    String sql = "SELECT * FROM parking WHERE owner_id=?";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setInt(1, ownerId);

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                ParkingData p = new ParkingData();

	                p.setParkingId(rs.getInt("parking_id"));
	                p.setOwnerId(rs.getInt("owner_id"));
	                p.setParkingName(rs.getString("parking_name"));
	                p.setLocation(rs.getString("location"));
	                p.setCity(rs.getString("city"));
	                p.setAddress(rs.getString("address"));
	                p.setTotalSlots(rs.getInt("total_slots"));
	                p.setPricePerHour(rs.getDouble("price_per_hour"));
	                p.setDescription(rs.getString("description"));
	                p.setStatus(rs.getString("status"));

	                list.add(p);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	public List<ParkingData> searchParking(String city, String vehicleType) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean addSlot(SlotData s) {
		    String sql = "INSERT INTO slots "
		            + "(parking_id, slot_number, vehicle_type, status) "
		            + "VALUES (?, ?, ?, ?)";

		    try (Connection con = myconnect.myconnection();
		         PreparedStatement ps = con.prepareStatement(sql)) {

		        ps.setInt(1, s.getParkingId());
		        ps.setString(2, s.getSlotNumber());
		        ps.setString(3, s.getVehicleType());
		        ps.setString(4, s.getStatus());

		        return ps.executeUpdate() > 0;

		    } catch (Exception e) {
		        e.printStackTrace();
		        return false;
		    }
		}

	public boolean updateSlot(SlotData s) {

	    String sql = "UPDATE slots SET slot_number=?, "
	            + "vehicle_type=?, status=? "
	            + "WHERE slot_id=? AND parking_id=?";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setString(1, s.getSlotNumber());
	        ps.setString(2, s.getVehicleType());
	        ps.setString(3, s.getStatus());
	        ps.setInt(4, s.getSlotId());
	        ps.setInt(5, s.getParkingId());

	        return ps.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean deleteSlot(int slotId, int parkingId) {
		String sql = "DELETE FROM slots "
	            + "WHERE slot_id=? AND parking_id=? "
	            + "AND status='AVAILABLE'";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setInt(1, slotId);
	        ps.setInt(2, parkingId);

	        return ps.executeUpdate() > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	
	public List<SlotData> getParkingSlots(int parkingId, String vehicleType) {
		List<SlotData> list = new ArrayList<>();

	    String sql = "SELECT * FROM slots WHERE parking_id=?";

	    if (vehicleType != null && !vehicleType.trim().isEmpty()) {
	        sql += " AND vehicle_type=?";
	    }

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setInt(1, parkingId);

	        if (vehicleType != null && !vehicleType.trim().isEmpty()) {
	            ps.setString(2, vehicleType);
	        }

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                SlotData s = new SlotData();

	                s.setSlotId(rs.getInt("slot_id"));
	                s.setParkingId(rs.getInt("parking_id"));
	                s.setSlotNumber(rs.getString("slot_number"));
	                s.setVehicleType(rs.getString("vehicle_type"));
	                s.setStatus(rs.getString("status"));

	                list.add(s);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	public boolean addVehicle(VehicleData vehicle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteVehicle(int vehicleId, int userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<VehicleData> getUserVehicles(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int createBooking(BookingData booking) {

    Connection con = null;

    try {
        // STEP 1: Read booking details
        int userId = booking.getUserId();
        int parkingId = booking.getParkingId();
        int slotId = booking.getSlotId();

        String vehicleNumber =
            booking.getVehicleNumber().trim().toUpperCase();

        String vehicleModel =
            booking.getVehicleModel().trim();

        String vehicleType =
            booking.getVehicleType().trim().toUpperCase();

        Timestamp startTime = booking.getStartTime();
        Timestamp endTime = booking.getEndTime();

        if (userId <= 0 || parkingId <= 0 || slotId <= 0 ||
            vehicleNumber.isEmpty() || vehicleModel.isEmpty() ||
            vehicleType.isEmpty() || startTime == null ||
            endTime == null || !endTime.after(startTime)) {

            throw new SQLException("Invalid booking details.");
        }

        // STEP 2: Connect to database
        con = mycon.myownconnection();
        System.out.println("CONNECTED DATABASE: " + con.getCatalog());

        if (con == null) {
            throw new SQLException("Database connection failed.");
        }

        con.setAutoCommit(false);

        int vehicleId = 0;
        int bookingId = 0;

        // STEP 3: Find existing vehicle by vehicle number
        // vehicle_number is UNIQUE in your database.
        String vehicleQuery =
            "SELECT vehicle_id, user_id FROM vehicles " +
            "WHERE vehicle_number = ? FOR UPDATE";

        try (PreparedStatement ps =
                 con.prepareStatement(vehicleQuery)) {

            ps.setString(1, vehicleNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    vehicleId = rs.getInt("vehicle_id");

                    int existingUserId = rs.getInt("user_id");

                    if (existingUserId != userId) {
                        throw new SQLException(
                            "This vehicle number belongs to another user."
                        );
                    }
                }
            }
        }

        // STEP 4: Insert vehicle if it does not exist
        if (vehicleId == 0) {

            String insertVehicle =
                "INSERT INTO vehicles " +
                "(user_id, vehicle_number, vehicle_type, vehicle_model) " +
                "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(
                    insertVehicle, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, userId);
                ps.setString(2, vehicleNumber);
                ps.setString(3, vehicleType);
                ps.setString(4, vehicleModel);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    throw new SQLException("Vehicle insertion failed.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        vehicleId = rs.getInt(1);
                    }
                }
            }
        }

        if (vehicleId <= 0) {
            throw new SQLException("Unable to get vehicle ID.");
        }

     // STEP 5: Check selected slot

        String slotQuery =
        	    "SELECT status, vehicle_type FROM owner_db.slots " +
        	    "WHERE slot_id = ? AND parking_id = ? FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(slotQuery)) {

            ps.setInt(1, slotId);
            ps.setInt(2, parkingId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "Selected slot does not exist for this parking."
                    );
                }

                String status = rs.getString("status");
                String dbVehicleType = rs.getString("vehicle_type");

                if (!"AVAILABLE".equalsIgnoreCase(status)) {
                    throw new SQLException("Selected slot is not available.");
                }

                if (!vehicleType.equalsIgnoreCase(dbVehicleType)) {
                    throw new SQLException("Vehicle type does not match.");
                }
            }
        }
        // STEP 6: Get parking price
        BigDecimal pricePerHour;
        String priceQuery =
        	    "SELECT price_per_hour FROM owner_db.parking " +
        	    "WHERE parking_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = con.prepareStatement(priceQuery)) {

            ps.setInt(1, parkingId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "Parking not found or parking is inactive."
                    );
                }

                pricePerHour = rs.getBigDecimal("price_per_hour");

                if (pricePerHour == null ||
                    pricePerHour.compareTo(BigDecimal.ZERO) < 0) {

                    throw new SQLException("Invalid parking price.");
                }
            }
        }

        // STEP 7: Calculate parking charges
        long durationMinutes =
            (endTime.getTime() - startTime.getTime()) / 60000;

        if (durationMinutes <= 0) {
            throw new SQLException("Invalid parking duration.");
        }

        BigDecimal durationHours =
            BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 4,
                        RoundingMode.HALF_UP);

        BigDecimal totalAmount =
            pricePerHour.multiply(durationHours)
                .setScale(2, RoundingMode.HALF_UP);

        // STEP 8: Insert booking
        String bookingQuery =
            "INSERT INTO bookings " +
            "(user_id, vehicle_id, parking_id, slot_id, " +
            "start_time, end_time, total_amount, booking_status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";

        try (PreparedStatement ps = con.prepareStatement(
                bookingQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, vehicleId);
            ps.setInt(3, parkingId);
            ps.setInt(4, slotId);
            ps.setTimestamp(5, startTime);
            ps.setTimestamp(6, endTime);
            ps.setBigDecimal(7, totalAmount);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Booking insertion failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
            }
        }

        if (bookingId <= 0) {
            throw new SQLException("Unable to generate booking ID.");
        }

        // STEP 9: Update slot status
        String updateSlot =
        	    "UPDATE owner_db.slots SET status = 'BOOKED' " +
        	    "WHERE slot_id = ? AND parking_id = ? " +
        	    "AND status = 'AVAILABLE'";

        try (PreparedStatement ps = con.prepareStatement(updateSlot)) {

            ps.setInt(1, slotId);
            ps.setInt(2, parkingId);

            int updatedRows = ps.executeUpdate();

            if (updatedRows != 1) {
                throw new SQLException(
                    "Unable to update selected slot."
                );
            }
        }

        // STEP 10: Commit
        con.commit();

        System.out.println("BOOKING SUCCESSFUL");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Vehicle ID: " + vehicleId);
        System.out.println("Total Amount: " + totalAmount);

        return bookingId;

    } catch (Exception e) {

        System.out.println("BOOKING ERROR: " + e.getMessage());
        e.printStackTrace();

        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return -1;

    } finally {

        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


	
	public boolean cancelBooking(int bookingId, int userId) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public List<BookingData> getUserBookings(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<BookingData> getOwnerBookings(int ownerId) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean updateBookingStatus(int bookingId, int ownerId, String status) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean addPayment(PaymentData payment) {
	    String query = "INSERT INTO payments "
	            + "(booking_id, amount, payment_method, transaction_id, "
	            + "payment_status, payment_date) "
	            + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

	    try (Connection con = mycon.myownconnection();
	         PreparedStatement ps = con.prepareStatement(query)) {

	        ps.setInt(1, payment.getBookingId());
	        ps.setDouble(2, payment.getAmount());
	        ps.setString(3, payment.getPaymentMethod());
	        ps.setString(4, payment.getTransactionId());
	        ps.setString(5, payment.getPaymentStatus());

	        int rows = ps.executeUpdate();

	        return rows > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	

public List<PaymentData> getOwnerPayments(int ownerId) {

    List<PaymentData> list = new ArrayList<>();

    String query =
        "SELECT " +
        "pay.payment_id, " +
        "pay.booking_id, " +
        "pay.amount, " +
        "pay.payment_method, " +
        "pay.transaction_id, " +
        "pay.payment_status, " +
        "pay.payment_date, " +
        "u.name AS customer_name, " +
        "v.vehicle_number, " +
        "v.vehicle_type, " +
        "COALESCE(vp.parking_name, op.parking_name) AS parking_name, " +
        "COALESCE(vs.slot_number, os.slot_number) AS slot_number, " +
        "b.start_time, " +
        "b.end_time " +

        "FROM payments pay " +

        "JOIN bookings b ON pay.booking_id = b.booking_id " +

        "LEFT JOIN users u ON b.user_id = u.user_id " +

        "LEFT JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +

        "LEFT JOIN parking vp ON " +
        "b.parking_id = vp.parking_id " +
        "AND b.parking_source = 'VEHICLE_DB' " +

        "LEFT JOIN owner_db.parking op ON " +
        "b.parking_id = op.parking_id " +
        "AND b.parking_source = 'OWNER_DB' " +

        "LEFT JOIN slots vs ON " +
        "b.slot_id = vs.slot_id " +
        "AND b.parking_id = vs.parking_id " +
        "AND b.parking_source = 'VEHICLE_DB' " +

        "LEFT JOIN owner_db.slots os ON " +
        "b.slot_id = os.slot_id " +
        "AND b.parking_id = os.parking_id " +
        "AND b.parking_source = 'OWNER_DB' " +

        "WHERE COALESCE(vp.owner_id, op.owner_id) = ? " +

        "ORDER BY pay.payment_id DESC";

    try (Connection con = mycon.myownconnection();
         PreparedStatement ps = con.prepareStatement(query)) {

        ps.setInt(1, ownerId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                PaymentData payment = new PaymentData();

                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));

                payment.setCustomerName(rs.getString("customer_name"));
                payment.setVehicleNumber(rs.getString("vehicle_number"));
                payment.setVehicleType(rs.getString("vehicle_type"));

                payment.setParkingName(rs.getString("parking_name"));
                payment.setSlotNumber(rs.getString("slot_number"));

                payment.setStartTime(rs.getTimestamp("start_time"));
                payment.setEndTime(rs.getTimestamp("end_time"));

                list.add(payment);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}

	
	public List<PaymentData> getUserPayments(int userId) {
		List<PaymentData> list = new ArrayList<>();

	    String query = "SELECT p.* FROM payments p "
	            + "JOIN bookings b ON p.booking_id = b.booking_id "
	            + "WHERE b.user_id = ? "
	            + "ORDER BY p.payment_id DESC";

	    try (Connection con = mycon.myownconnection();
	         PreparedStatement ps = con.prepareStatement(query)) {

	        ps.setInt(1, userId);

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                PaymentData payment = new PaymentData();

	                payment.setPaymentId(rs.getInt("payment_id"));
	                payment.setBookingId(rs.getInt("booking_id"));
	                payment.setAmount(rs.getDouble("amount"));
	                payment.setPaymentMethod(rs.getString("payment_method"));
	                payment.setTransactionId(rs.getString("transaction_id"));
	                payment.setPaymentStatus(rs.getString("payment_status"));
	                payment.setPaymentDate(rs.getTimestamp("payment_date"));

	                list.add(payment);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	public boolean addFeedback(FeedBackData feedback) {
		 String query = "INSERT INTO feedback "
		            + "(user_id, parking_id, rating, comments, feedback_date) "
		            + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

		    try (Connection con = mycon.myownconnection();
		         PreparedStatement ps = con.prepareStatement(query)) {

		        ps.setInt(1, feedback.getUserId());
		        ps.setInt(2, feedback.getParkingId());
		        ps.setInt(3, feedback.getRating());
		        ps.setString(4, feedback.getComments());

		        return ps.executeUpdate() > 0;

		    } catch (Exception e) {
		        e.printStackTrace();
		        return false;
		    }
		}

	
	public List<FeedBackData> getOwnerFeedback(int ownerId) {
		List<FeedBackData> list = new ArrayList<>();

	    String query = "SELECT * FROM feedback "
	            + "WHERE parking_id = ? "
	            + "ORDER BY feedback_id DESC";

	    try (Connection con = mycon.myownconnection();
	         PreparedStatement ps = con.prepareStatement(query)) {

	        ps.setInt(1, ownerId);

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                FeedBackData feedback = new FeedBackData();

	                feedback.setFeedbackId(rs.getInt("feedback_id"));
	                feedback.setUserId(rs.getInt("user_id"));
	                feedback.setParkingId(rs.getInt("parking_id"));
	                feedback.setRating(rs.getInt("rating"));
	                feedback.setComments(rs.getString("comments"));
	                feedback.setFeedbackDate(
	                    rs.getTimestamp("feedback_date")
	                );

	                list.add(feedback);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	public boolean addComplaint(ComplaintData complaint) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public List<ComplaintData> getUserComplaints(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ComplaintData> getOwnerComplaints(int ownerId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean updateComplaintStatus(int complaintId, int ownerId, String status) {
		// TODO Auto-generated method stub
		return false;
	}

	// ==========================================
	// USER: GET ALL ACTIVE PARKING LOCATIONS
	// ==========================================

	public List<ParkingData> getAllActiveParkings() {

	    List<ParkingData> list = new ArrayList<>();

	    String sql = "SELECT * FROM parking WHERE status='ACTIVE' ORDER BY parking_id DESC";

	    try (Connection con = myconnect.myconnection();
	         PreparedStatement ps = con.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        while (rs.next()) {

	            ParkingData p = new ParkingData();

	            p.setParkingId(rs.getInt("parking_id"));
	            p.setOwnerId(rs.getInt("owner_id"));
	            p.setParkingName(rs.getString("parking_name"));
	            p.setLocation(rs.getString("location"));
	            p.setCity(rs.getString("city"));
	            p.setAddress(rs.getString("address"));
	            p.setTotalSlots(rs.getInt("total_slots"));
	            p.setPricePerHour(rs.getDouble("price_per_hour"));
	            p.setDescription(rs.getString("description"));
	            p.setStatus(rs.getString("status"));

	            list.add(p);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return list;
	}


	// ==========================================
	// USER: GET SLOTS FOR A SELECTED PARKING
	// ==========================================

public List<SlotData> getPublicParkingSlots(int parkingId, String vehicleType) {

    List<SlotData> list = new ArrayList<>();

    String sql = "SELECT s.* FROM slots s " +
                 "JOIN parking p ON s.parking_id = p.parking_id " +
                 "WHERE s.parking_id = ? " +
                 "AND p.status = 'ACTIVE' " +
                 "AND s.vehicle_type = ? " +
                 "ORDER BY s.slot_id";

    try (Connection con = myconnect.myconnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, parkingId);
        ps.setString(2, vehicleType.toUpperCase());

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                SlotData slot = new SlotData();

                slot.setSlotId(rs.getInt("slot_id"));
                slot.setParkingId(rs.getInt("parking_id"));
                slot.setSlotNumber(rs.getString("slot_number"));
                slot.setVehicleType(rs.getString("vehicle_type"));
                slot.setStatus(rs.getString("status"));

                list.add(slot);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}


	
	
}
