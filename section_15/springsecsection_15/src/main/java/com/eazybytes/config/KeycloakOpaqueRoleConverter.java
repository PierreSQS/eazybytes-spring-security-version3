package com.eazybytes.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class KeycloakOpaqueRoleConverter implements OpaqueTokenAuthenticationConverter {
    @Override
    public Authentication convert(String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        // Extract roles from the authenticated principal
        Collection<GrantedAuthority> authorities = authenticatedPrincipal.getAuthorities();

        // Convert roles to GrantedAuthority
        List<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getAuthority()))
                .collect(Collectors.toList());

        // Create a new Authentication object with the converted roles
        return new UsernamePasswordAuthenticationToken(authenticatedPrincipal, null, grantedAuthorities);
    }
}
