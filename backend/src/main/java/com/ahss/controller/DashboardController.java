package com.ahss.controller;

import com.ahss.dto.response.ApiResponse;
import com.ahss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Use fully qualified Swagger annotations to avoid import issues

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Dashboard", description = "System statistics and recent activities")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class)))
})
public class DashboardController {

    @Autowired
    private UserService userService;

    @GetMapping("/stats")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get dashboard stats", description = "Retrieve system statistics and KPIs")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to retrieve dashboard statistics")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic statistics
            long totalUsers = userService.getAllActiveUsers().size();
            
            stats.put("totalUsers", totalUsers);
            stats.put("activeTenants", 5); // Mock data for now
            stats.put("totalRoles", 8); // Mock data for now
            stats.put("recentActivities", 12); // Mock data for now
            stats.put("systemHealth", "healthy");
            stats.put("pendingApprovals", 3); // Mock data for now
            
            return ResponseEntity.ok(ApiResponse.ok(stats, "Dashboard statistics retrieved successfully", "/api/v1/dashboard/stats"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.notOk(null, "Failed to retrieve dashboard statistics", "/api/v1/dashboard/stats"));
        }
    }

    @GetMapping("/recent-activities")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get recent activities", description = "Retrieve recent system activities")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to retrieve recent activities")
    })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // Mock recent activities data
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("id", "1");
            activity1.put("type", "user_login");
            activity1.put("description", "User admin@ahss.com logged in");
            activity1.put("timestamp", LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            activity1.put("user", "admin@ahss.com");
            activities.add(activity1);
            
            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("id", "2");
            activity2.put("type", "role_assigned");
            activity2.put("description", "Role 'Product Manager' assigned to user");
            activity2.put("timestamp", LocalDateTime.now().minusMinutes(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            activity2.put("user", "system");
            activities.add(activity2);
            
            Map<String, Object> activity3 = new HashMap<>();
            activity3.put("id", "3");
            activity3.put("type", "tenant_created");
            activity3.put("description", "New tenant 'ACME Corp' created");
            activity3.put("timestamp", LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            activity3.put("user", "admin@ahss.com");
            activities.add(activity3);
            
            Map<String, Object> activity4 = new HashMap<>();
            activity4.put("id", "4");
            activity4.put("type", "permission_granted");
            activity4.put("description", "Permission 'user:read' granted to role");
            activity4.put("timestamp", LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            activity4.put("user", "admin@ahss.com");
            activities.add(activity4);
            
            return ResponseEntity.ok(ApiResponse.ok(activities, "Recent activities retrieved successfully", "/api/v1/dashboard/recent-activities"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.notOk(null, "Failed to retrieve recent activities", "/api/v1/dashboard/recent-activities"));
        }
    }
}