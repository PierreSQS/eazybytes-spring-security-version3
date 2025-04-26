package com.eazybytes.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEvents {
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Authentication successful for user: {}", successEvent.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.error("Authentication failed for user: {}. Error: {}", failureEvent.getAuthentication().getName(),
                failureEvent.getException().getMessage());
    }

}
