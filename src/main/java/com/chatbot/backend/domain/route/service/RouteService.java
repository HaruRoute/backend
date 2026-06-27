package com.chatbot.backend.domain.route.service;

import com.chatbot.backend.domain.route.dto.RouteLocationDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    private static final double EARTH_RADIUS = 6371.0; // 지구 반지름 (km)

    @Cacheable(value = "route", key = "#requests.stream().map(r -> r.getLat() + ',' + r.getLng()).collect(T(java.util.stream.Collectors).joining('|'))")
    public RouteLocationDto.OptimizedRoute optimizeRoute(List<RouteLocationDto.Request> requests) {
        if (requests == null || requests.isEmpty()) {
            return RouteLocationDto.OptimizedRoute.builder()
                    .route(new ArrayList<>())
                    .totalDistance(0.0)
                    .build();
        }

        if (requests.size() == 1) {
            RouteLocationDto.Request first = requests.get(0);
            List<RouteLocationDto.Response> route = new ArrayList<>();
            route.add(toResponse(first, 0.0));
            return RouteLocationDto.OptimizedRoute.builder()
                    .route(route)
                    .totalDistance(0.0)
                    .build();
        }

        List<RouteLocationDto.Request> optimizedList;
        if (requests.size() <= 10) {
            optimizedList = solveTSPExact(requests);
        } else {
            optimizedList = solveTSPNearestNeighbor(requests);
        }

        List<RouteLocationDto.Response> routeResponses = new ArrayList<>();
        double totalDistance = 0.0;

        // 첫 번째 지점은 이전 지점으로부터 거리가 0.0
        routeResponses.add(toResponse(optimizedList.get(0), 0.0));

        for (int i = 1; i < optimizedList.size(); i++) {
            RouteLocationDto.Request prev = optimizedList.get(i - 1);
            RouteLocationDto.Request curr = optimizedList.get(i);
            double dist = calculateDistance(prev.getLat(), prev.getLng(), curr.getLat(), curr.getLng());
            totalDistance += dist;
            routeResponses.add(toResponse(curr, Math.round(dist * 100.0) / 100.0));
        }

        return RouteLocationDto.OptimizedRoute.builder()
                .route(routeResponses)
                .totalDistance(Math.round(totalDistance * 100.0) / 100.0)
                .build();
    }

    private RouteLocationDto.Response toResponse(RouteLocationDto.Request req, double dist) {
        return RouteLocationDto.Response.builder()
                .title(req.getTitle())
                .lat(req.getLat())
                .lng(req.getLng())
                .addr1(req.getAddr1())
                .firstimage(req.getFirstimage())
                .distanceFromPrevious(dist)
                .build();
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) return 0.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    // 백트래킹을 통한 Exact TSP 해결 (출발지는 인덱스 0 고정, open path)
    private List<RouteLocationDto.Request> solveTSPExact(List<RouteLocationDto.Request> requests) {
        int n = requests.size();
        double[][] distMatrix = buildDistanceMatrix(requests);

        int[] bestPath = new int[n];
        double[] minCost = {Double.MAX_VALUE};

        int[] currentPath = new int[n];
        boolean[] visited = new boolean[n];

        currentPath[0] = 0;
        visited[0] = true;

        backtrack(1, 0, 0.0, currentPath, visited, distMatrix, bestPath, minCost);

        List<RouteLocationDto.Request> result = new ArrayList<>();
        for (int idx : bestPath) {
            result.add(requests.get(idx));
        }
        return result;
    }

    private void backtrack(int depth, int lastNode, double currentCost, int[] currentPath,
                           boolean[] visited, double[][] distMatrix, int[] bestPath, double[] minCost) {
        int n = distMatrix.length;

        if (currentCost >= minCost[0]) {
            return;
        }

        if (depth == n) {
            minCost[0] = currentCost;
            System.arraycopy(currentPath, 0, bestPath, 0, n);
            return;
        }

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                visited[i] = true;
                currentPath[depth] = i;
                backtrack(depth + 1, i, currentCost + distMatrix[lastNode][i],
                        currentPath, visited, distMatrix, bestPath, minCost);
                visited[i] = false;
            }
        }
    }

    // Nearest Neighbor 휴리스틱 (출발지는 인덱스 0 고정, open path)
    private List<RouteLocationDto.Request> solveTSPNearestNeighbor(List<RouteLocationDto.Request> requests) {
        int n = requests.size();
        List<RouteLocationDto.Request> result = new ArrayList<>();
        boolean[] visited = new boolean[n];

        result.add(requests.get(0));
        visited[0] = true;

        int currentIdx = 0;
        for (int step = 1; step < n; step++) {
            int nearestIdx = -1;
            double minDist = Double.MAX_VALUE;

            RouteLocationDto.Request current = requests.get(currentIdx);

            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    double dist = calculateDistance(current.getLat(), current.getLng(),
                            requests.get(i).getLat(), requests.get(i).getLng());
                    if (dist < minDist) {
                        minDist = dist;
                        nearestIdx = i;
                    }
                }
            }

            if (nearestIdx != -1) {
                visited[nearestIdx] = true;
                result.add(requests.get(nearestIdx));
                currentIdx = nearestIdx;
            }
        }

        return result;
    }

    private double[][] buildDistanceMatrix(List<RouteLocationDto.Request> requests) {
        int n = requests.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = calculateDistance(
                            requests.get(i).getLat(), requests.get(i).getLng(),
                            requests.get(j).getLat(), requests.get(j).getLng()
                    );
                }
            }
        }
        return matrix;
    }
}
