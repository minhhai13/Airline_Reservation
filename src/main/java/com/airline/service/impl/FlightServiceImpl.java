package com.airline.service.impl;

import com.airline.dao.FlightDAO;
import com.airline.dao.RouteDAO;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    @Autowired
    private FlightDAO flightDAO;

    @Autowired
    private RouteDAO routeDAO;

    @Override
    public Flight createFlight(Flight flight) {
        return flightDAO.save(flight);
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
    public List<Flight> findAll() {
        return flightDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAvailableFlights() {
        return flightDAO.findAvailableFlights();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> searchFlights(String origin, String destination, LocalDate date) {
        Optional<Route> routeOpt = routeDAO.findByOriginAndDestination(origin, destination);
        
        if (routeOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        Route route = routeOpt.get();
        LocalDateTime startOfDay = date.atStartOfDay();
        
        return flightDAO.findByRouteAndDate(route.getId(), startOfDay);
    }

    @Override
    public Flight updateFlight(Flight flight) {
        return flightDAO.save(flight);
    }

    @Override
    public void deleteFlight(Long id) {
        flightDAO.findById(id).ifPresent(flightDAO::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableSeats(Long flightId, int requiredSeats) {
        Optional<Flight> flightOpt = flightDAO.findById(flightId);
        return flightOpt.map(f -> f.hasAvailableSeats(requiredSeats)).orElse(false);
    }
}

