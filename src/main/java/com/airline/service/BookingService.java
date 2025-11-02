package com.airline.service;

import com.airline.dto.BookingRequestDTO;
import com.airline.dto.BookingResponseDTO;
import com.airline.entity.Booking;
import java.util.List;
import java.util.Optional;

/**
 * BookingService Interface
 */
public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO requestDTO);

    Booking confirmBooking(Long bookingId);

    Booking cancelBooking(Long bookingId);

    Optional<Booking> findById(Long id);

    List<Booking> findByUserId(Long userId);

    List<Booking> findAllBookings();

    List<Booking> findByStatus(Booking.BookingStatus status);
}
