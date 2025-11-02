package com.airline.service;

import com.airline.dto.FlightSearchDTO;
import com.airline.entity.Flight;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FlightService Interface
 */
public interface FlightService {

    Flight createFlight(Flight flight);

    Flight updateFlight(Flight flight);

    void deleteFlight(Long id);

    Optional<Flight> findById(Long id);

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findAllFlights();

    List<Flight> findAvailableFlights();

    List<Flight> searchFlights(FlightSearchDTO searchDTO);

    List<Flight> findByRouteAndDate(Long routeId, LocalDateTime date);

    boolean hasAvailableSeats(Long flightId, int requiredSeats);
}
