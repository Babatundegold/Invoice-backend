package com.invoicesaas.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** Small helper to pull the logged-in user's id out of the security context in controllers. */
public class CurrentUser {
    public static Long id() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
