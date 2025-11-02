// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.service.impl;

import com.airline.dao.FlightDAO;
import com.airline.dao.RouteDAO;
import com.airline.dto.FlightSearchDTO;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

/**
 * FlightServiceImpl
 */
@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightDAO flightDAO;
    private final RouteDAO routeDAO;

    @Autowired
    public FlightServiceImpl(FlightDAO flightDAO, RouteDAO routeDAO) {
        this.flightDAO = flightDAO;
        this.routeDAO = routeDAO;
    }

    @Override
    public Flight createFlight(Flight flight) {
        // Validate flight number uniqueness
        if (flightDAO.findByFlightNumber(flight.getFlightNumber()).isPresent()) {
            throw new IllegalArgumentException("Flight number already exists");
        }

        // Validate route exists
        if (flight.getRoute() == null || flight.getRoute().getId() == null) {
            throw new IllegalArgumentException("Route is required");
        }

        // Validate aircraft exists
        if (flight.getAircraft() == null || flight.getAircraft().getId() == null) {
            throw new IllegalArgumentException("Aircraft is required");
        }

        return flightDAO.save(flight);
    }

    @Override
    public Flight updateFlight(Flight flight) {
        Flight existingFlight = flightDAO.findById(flight.getId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        // Check if flight number is being changed and if it conflicts
        if (!existingFlight.getFlightNumber().equals(flight.getFlightNumber())) {
            if (flightDAO.findByFlightNumber(flight.getFlightNumber()).isPresent()) {
                throw new IllegalArgumentException("Flight number already exists");
            }
        }

        return flightDAO.save(flight);
    }

    @Override
    public void deleteFlight(Long id) {
        Flight flight = flightDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        flightDAO.delete(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Flight> findById(Long id) {
        return flightDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return flightDAO.findByFlightNumber(flightNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAllFlights() {
        return flightDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAvailableFlights() {
        return flightDAO.findAvailableFlights();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> searchFlights(FlightSearchDTO searchDTO) {
        // Find route by origin and destination
        Optional<Route> routeOpt = routeDAO.findByOriginAndDestination(
                searchDTO.getOrigin(),
                searchDTO.getDestination()
        );

        if (routeOpt.isEmpty()) {
            return List.of(); // No route found
        }

        Route route = routeOpt.get();
        List<Flight> flights = flightDAO.findByRouteAndDate(route.getId(), searchDTO.getDepartureDate());

        // Filter by available seats
        return flights.stream()
                .filter(f -> f.hasAvailableSeats(searchDTO.getPassengers()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findByRouteAndDate(Long routeId, LocalDateTime date) {
        return flightDAO.findByRouteAndDate(routeId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableSeats(Long flightId, int requiredSeats) {
        Optional<Flight> flightOpt = flightDAO.findById(flightId);
        return flightOpt.isPresent() && flightOpt.get().hasAvailableSeats(requiredSeats);
    }
}
