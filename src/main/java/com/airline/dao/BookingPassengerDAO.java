// ========================================
// BookingPassengerDAO
// ========================================
package com.airline.dao;

import com.airline.entity.BookingPassenger;
import java.util.List;
import java.util.Optional;

public interface BookingPassengerDAO {

    BookingPassenger save(BookingPassenger passenger);

    Optional<BookingPassenger> findById(Long id);

    List<BookingPassenger> findByBookingId(Long bookingId);

    void delete(BookingPassenger passenger);
}
