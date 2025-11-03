package com.airline.service.impl;

import com.airline.dao.BookingDAO;
import com.airline.dao.FlightDAO;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private FlightDAO flightDAO;

    /**
     * Rule-based AI: Recommend flights based on user's booking history
     * Logic:
     * 1. Find all CONFIRMED bookings of the user
     * 2. Get the 3 most frequent routes
     * 3. Find upcoming flights on those routes
     */
    @Override
    public List<Flight> getRecommendedFlights(Long userId, int limit) {
        // Get user's confirmed bookings
        List<Booking> userBookings = bookingDAO.findByUserId(userId).stream()
            .filter(Booking::isConfirmed)
            .collect(Collectors.toList());
        
        if (userBookings.isEmpty()) {
            // No history -> return popular upcoming flights
            return flightDAO.findAvailableFlights().stream()
                .filter(f -> f.getDepartureTime().isAfter(LocalDateTime.now()))
                .limit(limit)
                .collect(Collectors.toList());
        }
        
        // Count route frequency
        Map<Long, Long> routeFrequency = userBookings.stream()
            .map(b -> b.getFlight().getRoute().getId())
            .collect(Collectors.groupingBy(
                routeId -> routeId,
                Collectors.counting()
            ));
        
        // Get top 3 routes
        List<Long> topRoutes = routeFrequency.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Find upcoming flights on these routes
        List<Flight> recommendedFlights = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Long routeId : topRoutes) {
            List<Flight> routeFlights = flightDAO.findByRouteId(routeId).stream()
                .filter(f -> f.getDepartureTime().isAfter(now))
                .filter(Flight::hasAvailableSeats)
                .limit(2)
                .collect(Collectors.toList());
            
            recommendedFlights.addAll(routeFlights);
        }
        
        return recommendedFlights.stream()
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
}

