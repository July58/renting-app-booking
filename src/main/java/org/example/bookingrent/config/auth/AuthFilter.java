package org.example.bookingrent.config.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        String path = request.getRequestURI();
        if (path.startsWith("/dapr/subscribe")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        token = token.substring(7);

        try {

            byte[] respBytes = daprClient.invokeMethod(
                    "auth-service",
                    "api/validate-token",
                    token,
                    HttpExtension.POST,
                    byte[].class
            ).block();

            JsonNode body = objectMapper.readTree(respBytes);
            JsonNode userData = body.path("data").path("user");
            if (userData.isMissingNode()) {
                throw new RuntimeException("Invalid token");
            }


            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userData, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
