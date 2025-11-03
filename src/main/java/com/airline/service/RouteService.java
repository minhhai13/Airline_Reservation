package com.airline.service;

import com.airline.entity.Route;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    Route createRoute(Route route);
    Optional<Route> findById(Long id);
    Optional<Route> findByOriginAndDestination(String origin, String destination);
    List<Route> findAll();
    List<String> getAllOrigins();
    List<String> getAllDestinations();
}

