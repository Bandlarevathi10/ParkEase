package com.model.design;

import java.util.List;

import com.model.data.BookingData;
import com.model.data.ComplaintData;
import com.model.data.FeedBackData;
import com.model.data.ParkingData;
import com.model.data.PaymentData;
import com.model.data.SlotData;
import com.model.data.Userdataa;
import com.model.data.VehicleData;

public interface Userdesign {
	


    // USER MODULE
    boolean registerUser(Userdataa user);
    Userdataa loginUser(String email, String password);
    Userdataa getUserProfile(int userId);
    boolean updateUserProfile(Userdataa user);

    // PARKING MODULE
    boolean addParking(ParkingData parking);
    boolean updateParking(ParkingData parking);
    boolean deleteParking(int parkingId, int ownerId);
    List<ParkingData> getOwnerParking(int ownerId);
    List<ParkingData> searchParking(String city, String vehicleType);
    boolean addParkingWithSlots(ParkingData p, int ownerId,
            int carSlots, int bikeSlots);

    // SLOT MODULE
    boolean addSlot(SlotData slot);
    boolean updateSlot(SlotData slot);
    boolean deleteSlot(int slotId, int parkingId);
    List<SlotData> getParkingSlots(int parkingId, String vehicleType);

    // VEHICLE MODULE
    boolean addVehicle(VehicleData vehicle);
    boolean deleteVehicle(int vehicleId, int userId);
    List<VehicleData> getUserVehicles(int userId);

    // BOOKING MODULE
    int createBooking(BookingData booking);
    boolean cancelBooking(int bookingId, int userId);
    List<BookingData> getUserBookings(int userId);
    List<BookingData> getOwnerBookings(int ownerId);
    boolean updateBookingStatus(int bookingId, int ownerId, String status);

    // PAYMENT MODULE
    boolean addPayment(PaymentData payment);
    List<PaymentData> getOwnerPayments(int ownerId);
    List<PaymentData> getUserPayments(int userId);

    // FEEDBACK MODULE
    boolean addFeedback(FeedBackData feedback);
    List<FeedBackData> getOwnerFeedback(int ownerId);

    // COMPLAINT MODULE
    boolean addComplaint(ComplaintData complaint);
    List<ComplaintData> getUserComplaints(int userId);
    List<ComplaintData> getOwnerComplaints(int ownerId);
    boolean updateComplaintStatus(int complaintId, int ownerId, String status);
}
