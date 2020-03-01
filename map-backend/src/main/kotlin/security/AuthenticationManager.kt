package com.apxeolog.hnh.mapbackend.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/**
 * @author APXEOLOG (Artyom Melnikov), at 19.03.2019
 */

@Component
class CustomAuthenticationManager() : AuthenticationManager {
    override fun authenticate(authentication: Authentication?): Authentication {
        return UsernamePasswordAuthenticationToken("", "")
    }
}