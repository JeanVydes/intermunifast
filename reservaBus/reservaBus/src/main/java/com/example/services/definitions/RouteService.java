package com.example.services.definitions;

import java.util.List;

import com.example.api.dto.RouteDTOs;
import com.example.api.dto.StopDTOs;

public interface RouteService {
    RouteDTOs.RouteResponse createRoute(RouteDTOs.CreateRouteRequest req);

    RouteDTOs.RouteResponse getRouteById(Long id);

    RouteDTOs.RouteResponse updateRoute(Long id, RouteDTOs.UpdateRouteRequest req);

    void deleteRoute(Long id);

    List<StopDTOs.StopResponse> getStopsByRouteId(Long id);

    List<RouteDTOs.RouteResponse> findByOrigin(String origin);
    List<RouteDTOs.RouteResponse> findByDestination(String destination);
    List<RouteDTOs.RouteResponse> findByOriginAndDestination(String origin, String destination);
}
