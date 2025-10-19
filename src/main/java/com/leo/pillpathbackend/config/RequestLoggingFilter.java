package com.leo.pillpathbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        System.out.println("📨 === INCOMING REQUEST ===");
        System.out.println("📨 Method: " + httpRequest.getMethod());
        System.out.println("📨 URI: " + httpRequest.getRequestURI());
        System.out.println("📨 Query: " + httpRequest.getQueryString());
        System.out.println("📨 Content-Type: " + httpRequest.getHeader("Content-Type"));
        System.out.println("📨 Origin: " + httpRequest.getHeader("Origin"));
        System.out.println("📨 Authorization: " + (httpRequest.getHeader("Authorization") != null ? "Present" : "None"));
        System.out.println("📨 Remote Addr: " + httpRequest.getRemoteAddr());
        System.out.println("📨 ========================");
        
        chain.doFilter(request, response);
    }
}
