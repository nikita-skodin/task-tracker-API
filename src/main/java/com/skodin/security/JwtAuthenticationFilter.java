package com.skodin.security;

import com.skodin.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            String jwt = ((HttpServletRequest) servletRequest).getHeader("Authorization");

            if (jwt != null && jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);

                if (jwtService.isTokenValid(jwt)) {

                    String username = jwtService.extractUsername(jwt);
                    userDetailsService.loadUserByUsername(username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            jwtService.extractAllClaims(jwt),
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            // TODO: 016 попробовать перенести до филтер сюда  
        } catch (Exception ignored) {

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
