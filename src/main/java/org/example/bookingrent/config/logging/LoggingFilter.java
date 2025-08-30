package org.example.bookingrent.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class LoggingFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        logger.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());

        chain.doFilter(request, response);

        logger.info("Completed request: {} {}", req.getMethod(), req.getRequestURI());
    }
}

