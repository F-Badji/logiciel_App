package com.logicielapp.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import com.logicielapp.service.StatisticsService;
import java.util.*;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    
    public StatisticsController() {
        this.statisticsService = new StatisticsService();
    }

    @GetMapping
    public String statisticsPage() {
        return "statistiques";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics() {
        // Récupérer les vraies statistiques depuis la base de données
        Map<String, Object> stats = statisticsService.getRealStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/recent-activity")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        // Récupérer les vraies activités récentes depuis la base de données
        List<Map<String, Object>> activities = statisticsService.getRealRecentActivity();
        return ResponseEntity.ok(activities);
    }

}
