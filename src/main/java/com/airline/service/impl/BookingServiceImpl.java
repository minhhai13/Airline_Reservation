package com.airline.service.impl;

import com.airline.dao.BookingDAO;
import com.airline.dao.FlightDAO;
import com.airline.dao.UserDAO;
import com.airline.entity.Booking;
import com.airline.entity.BookingPassenger;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private FlightDAO flightDAO;

    @Override
    public Booking createBooking(Long userId, Long flightId, List<BookingPassenger> passengers) {
        User user = userDAO.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Flight flight = flightDAO.findById(flightId)
            .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        
        int passengerCount = passengers.size();
        
        // Check available seats
        if (!flight.hasAvailableSeats(passengerCount)) {
            throw new IllegalStateException("Not enough available seats");
        }
        
        // Calculate total price
        BigDecimal totalPrice = flight.getPrice()
            .multiply(BigDecimal.valueOf(passengerCount));
        
        // Create booking
        Booking booking = Booking.builder()
            .user(user)
            .flight(flight)
            .totalPrice(totalPrice)
            .status(Booking.BookingStatus.PENDING)
            .build();
        
        // Add passengers
        for (BookingPassenger passenger : passengers) {
            booking.addPassenger(passenger);
        }
        
        // Decrease available seats
        flight.decreaseAvailableSeats(passengerCount);
        flightDAO.save(flight);
        
        return bookingDAO.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByUserId(Long userId) {
        return bookingDAO.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByFlightId(Long flightId) {
        return bookingDAO.findByFlightId(flightId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingDAO.findAll();
    }

    @Override
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.confirm();
        return bookingDAO.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.cancel();
        
        // Return seats to flight
        Flight flight = booking.getFlight();
        flight.increaseAvailableSeats(booking.getPassengerCount());
        flightDAO.save(flight);
        
        return bookingDAO.save(booking);
    }

    @Override
    public void deleteBooking(Long id) {
        bookingDAO.findById(id).ifPresent(bookingDAO::delete);
    }
}

