package com.bot.apigateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@jakarta.servlet.annotation.WebFilter(urlPatterns = "/*")
public class AuthenticationFilter implements WebFilter {
    @Autowired
    private RouteValidator routeValidator;
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        LOGGER.info("[API REQUEST]: " + exchange.getRequest().getURI().getPath());
        if (HttpMethod.OPTIONS.matches(String.valueOf(exchange.getRequest().getMethod()))) {
            return chain.filter(exchange); // Skip authentication for OPTIONS requests
        }

        ServerWebExchange modifiedExchange = exchange;
        if (routeValidator.isSecured.test(exchange.getRequest())) {
            // check Authorizatrion header -- ServerWebExchange
            LOGGER.info("[URL REQUESTED]: " + exchange.getRequest().getURI().getPath());
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Unauthorization access. Token is missing.");
            }

            String authorizationHeader = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
                authorizationHeader = authorizationHeader.substring(7);
            }

            try {
                String secret = "bottomhalfx12#@-common-service-for-token-gen-server-ems-x#20$46@3211";
                byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);

//                Claims claims = Jwts.parser()
//                        .setSigningKey(key)
//                        .parseClaimsJws(authorizationHeader)
//                        .getBody();
//
//                String sid = claims.get("sid", String.class);
//                String user = claims.get("JBot", String.class);
//                String companyCode = claims.get("CompanyCode", String.class);
//
//                modifiedExchange = exchange.mutate()
//                        .request(exchange.getRequest().mutate()
//                                .headers(httpHeaders -> httpHeaders.add("userDetail", user))
//                                .headers(httpHeaders -> httpHeaders.add("sid", sid))
//                                .headers(httpHeaders -> httpHeaders.add("companyCode", companyCode))
//                                .build())
//                        .build();

            } catch (ExpiredJwtException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your session got expired");
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unauthorized access. Please try with valid token.");
            }
        }

        return chain.filter(modifiedExchange);
    }
}
