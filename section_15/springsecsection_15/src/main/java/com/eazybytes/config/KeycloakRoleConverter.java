package com.eazybytes.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KeycloakRoleConverter is a custom converter that converts JWT tokens into a collection of GrantedAuthority.
 * It extracts roles from the JWT token and maps them to Spring Security's GrantedAuthority.
 * <p>
 * // Extract roles from the JWT token
 * <p>
 * // and convert them to GrantedAuthority objects
 * <p>
 *         // For example:
 * <p>
 *         List<GrantedAuthority> authorities = new ArrayList<>();
 * <p>
 *         List<String> roles = (List<String>) source.getClaims().get("roles");
 * <p>
 *         for (String role : roles) {
 * <p>
 *           // Convert each role to a GrantedAuthority
 *           authorities.add("ROLE_"+new SimpleGrantedAuthority(role));
 * <p>
 *         }
 * <p>
 *         return authorities;
 *
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwtToken) {
        Object realmAccess = jwtToken.getClaim("realm_access");

        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            List<String> roles = (List<String>) realmAccessMap.get("roles");
            if (roles != null) {
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
