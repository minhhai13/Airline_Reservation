package com.airline.service;

import com.airline.entity.Flight;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FlightService {

    Flight createFlight(Flight flight);

    Optional<Flight> findById(Long id);

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findAll();

    List<Flight> findAvailableFlights();

    List<Flight> searchFlights(String origin, String destination, LocalDate date);

    Flight updateFlight(Flight flight);

    void deleteFlight(Long id);

    boolean hasAvailableSeats(Long flightId, int requiredSeats);

    Map<String, Object> searchFlightsWithPaging(String origin, String destination, LocalDate date, int page, int size);

}
