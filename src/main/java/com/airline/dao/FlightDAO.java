package com.airline.dao;

import com.airline.entity.Flight;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FlightDAO Interface
 */
public interface FlightDAO {

    Flight save(Flight flight);

    Optional<Flight> findById(Long id);

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findAll();

    List<Flight> findByRouteId(Long routeId);

    List<Flight> findByRouteAndDate(Long routeId, LocalDateTime date);

    List<Flight> findAvailableFlights();

    void delete(Flight flight);
}
