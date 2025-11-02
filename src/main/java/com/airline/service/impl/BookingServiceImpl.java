// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.service.impl;

import com.airline.dao.*;
import com.airline.dto.BookingRequestDTO;
import com.airline.dto.BookingResponseDTO;
import com.airline.dto.PassengerInfoDTO;
import com.airline.entity.*;
import com.airline.exception.BookingException;
import com.airline.exception.ResourceNotFoundException;
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

    private final BookingDAO bookingDAO;
    private final FlightDAO flightDAO;
    private final UserDAO userDAO;
    private final BookingPassengerDAO passengerDAO;

    @Autowired
    public BookingServiceImpl(BookingDAO bookingDAO,
            FlightDAO flightDAO,
            UserDAO userDAO,
            BookingPassengerDAO passengerDAO) {
        this.bookingDAO = bookingDAO;
        this.flightDAO = flightDAO;
        this.userDAO = userDAO;
        this.passengerDAO = passengerDAO;
    }

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO) {
        // 1. Validate user exists
        User user = userDAO.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Validate flight exists and has available seats
        Flight flight = flightDAO.findById(requestDTO.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        int passengerCount = requestDTO.getPassengers().size();
        if (!flight.hasAvailableSeats(passengerCount)) {
            throw new BookingException("Not enough available seats");
        }

        // 3. Calculate total price
        BigDecimal totalPrice = flight.getPrice()
                .multiply(BigDecimal.valueOf(passengerCount));

        // 4. Create booking
        Booking booking = Booking.builder()
                .user(user)
                .flight(flight)
                .status(Booking.BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .build();

        // 5. Add passengers to booking
        for (PassengerInfoDTO passengerDTO : requestDTO.getPassengers()) {
            BookingPassenger passenger = BookingPassenger.builder()
                    .fullName(passengerDTO.getFullName())
                    .email(passengerDTO.getEmail())
                    .phone(passengerDTO.getPhone())
                    .seatNumber(passengerDTO.getSeatNumber())
                    .booking(booking)
                    .build();
            booking.addPassenger(passenger);
        }

        // 6. Decrease available seats (ATOMIC with booking creation)
        flight.decreaseAvailableSeats(passengerCount);
        flightDAO.save(flight);

        // 7. Save booking (will cascade save passengers)
        Booking savedBooking = bookingDAO.save(booking);

        // 8. Build response
        return buildBookingResponse(savedBooking);
    }

    @Override
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.confirm();
        return bookingDAO.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Restore available seats (ATOMIC operation)
        Flight flight = booking.getFlight();
        int passengerCount = booking.getPassengerCount();
        flight.increaseAvailableSeats(passengerCount);
        flightDAO.save(flight);

        // Cancel booking
        booking.cancel();
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
    public List<Booking> findAllBookings() {
        return bookingDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByStatus(Booking.BookingStatus status) {
        return bookingDAO.findByStatus(status);
    }

    // Helper method
    private BookingResponseDTO buildBookingResponse(Booking booking) {
        Flight flight = booking.getFlight();
        Route route = flight.getRoute();

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .flightNumber(flight.getFlightNumber())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .bookingStatus(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .passengerCount(booking.getPassengerCount())
                .bookingDate(booking.getBookingDate())
                .build();
    }
}
