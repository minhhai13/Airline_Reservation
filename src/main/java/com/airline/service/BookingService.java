package com.airline.service;

import com.airline.entity.Booking;
import com.airline.entity.BookingPassenger;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(Long userId, Long flightId, List<BookingPassenger> passengers);
    Optional<Booking> findById(Long id);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByFlightId(Long flightId);
    List<Booking> findAll();
    Booking confirmBooking(Long bookingId);
    Booking cancelBooking(Long bookingId);
    void deleteBooking(Long id);
}

