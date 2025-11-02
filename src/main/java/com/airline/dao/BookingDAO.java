package com.airline.dao;

import com.airline.entity.Booking;
import java.util.List;
import java.util.Optional;

/**
 * BookingDAO Interface
 */
public interface BookingDAO {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    List<Booking> findByUserId(Long userId);

    List<Booking> findByFlightId(Long flightId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    void delete(Booking booking);
}
