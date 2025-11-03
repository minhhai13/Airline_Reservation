package com.airline.service.impl;

import com.airline.dao.RouteDAO;
import com.airline.entity.Route;
import com.airline.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RouteServiceImpl implements RouteService {

    @Autowired
    private RouteDAO routeDAO;

    @Override
    public Route createRoute(Route route) {
        return routeDAO.save(route);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Route> findById(Long id) {
        return routeDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Route> findByOriginAndDestination(String origin, String destination) {
        return routeDAO.findByOriginAndDestination(origin, destination);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllOrigins() {
        return routeDAO.findAll().stream()
            .map(Route::getOrigin)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDestinations() {
        return routeDAO.findAll().stream()
            .map(Route::getDestination)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}

