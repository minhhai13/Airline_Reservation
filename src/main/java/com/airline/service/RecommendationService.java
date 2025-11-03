package com.airline.service;

import com.airline.entity.Flight;
import java.util.List;

public interface RecommendationService {
    List<Flight> getRecommendedFlights(Long userId, int limit);
}

