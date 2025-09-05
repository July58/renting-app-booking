package org.example.bookingrent.config.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bookingrent.req_res.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final DaprClient daprClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        if (request.getRequestURI().startsWith("/dapr/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }


        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", token);

            byte[] respBytes = daprClient.invokeMethod(
                    "auth-service",
                    "api/validate-token",
                    "",
                    HttpExtension.POST,
                    headers,
                    byte[].class
            ).block();


            if (respBytes == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }


            JsonNode body = objectMapper.readTree(respBytes);

            if (body.has("isValid") && !body.get("isValid").asBoolean()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token validation failed");
                return;
            }

            JsonNode userData = body.has("user") ? body.get("user") :
                    (body.has("data") && body.get("data").has("user") ? body.get("data").get("user") :
                            body.has("principal") ? body.get("principal") : null);

            if (userData == null || userData.isNull()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User data not found in auth response");
                return;
            }


            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userData, token, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ApiResponse<Void> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Authentication service is unavailable");
            apiResponse.setData(null);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/dapr/")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/actuator/metrics")
                || path.startsWith("/actuator/loggers");
    }
}
