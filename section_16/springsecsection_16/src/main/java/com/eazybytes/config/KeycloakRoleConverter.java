package com.eazybytes.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    /**
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return Collection of {@link GrantedAuthority} if the conversion was successful,
     * or {@code null} if the conversion failed
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        ArrayList<String> roles = (ArrayList<String>) source.getClaims().get("scope");
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return roles.stream().map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
