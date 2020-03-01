package com.apxeolog.hnh.mapbackend.security

import com.apxeolog.hnh.mapbackend.user.CustomPrincipal
import com.apxeolog.hnh.mapbackend.user.User
import com.apxeolog.hnh.mapbackend.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * @author APXEOLOG (Artyom Melnikov), at 19.03.2019
 */
private const val AUTH_HEADER: String = "Authentication"

@Component
class AuthenticationFilter(val userRepository: UserRepository,
                           authenticationManager: AuthenticationManager) : BasicAuthenticationFilter(authenticationManager) {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        request.getHeader(AUTH_HEADER)?.let { token ->
            userRepository.findUserByToken(token)?.let { user ->
                authenticate(request, user)
            }
        }
        chain.doFilter(request, response)
    }

    private fun authenticate(request: HttpServletRequest, user: User) {
        val authentication = UsernamePasswordAuthenticationToken(CustomPrincipal(user), "whatever")
        authentication.isAuthenticated = true

        val securityContext = SecurityContextHolder.getContext()
        securityContext.authentication = authentication
        val session = request.getSession(true)
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext)
    }
}