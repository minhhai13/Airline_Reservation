package com.airline.service;

import com.airline.entity.Aircraft;
import com.airline.entity.Route;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AdminService Interface Business logic for admin operations
 */
public interface AdminService {

    // Dashboard Statistics
    Map<String, Object> getDashboardStatistics();

    // Aircraft Management
    Aircraft createAircraft(Aircraft aircraft);

    Aircraft updateAircraft(Aircraft aircraft);

    void deleteAircraft(Long id);

    List<Aircraft> findAllAircrafts();

    // Route Management
    Route createRoute(Route route);

    Route updateRoute(Route route);

    void deleteRoute(Long id);

    List<Route> findAllRoutes();
}
