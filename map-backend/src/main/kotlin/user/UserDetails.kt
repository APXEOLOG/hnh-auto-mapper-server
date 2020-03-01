package com.apxeolog.hnh.mapbackend.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

/**
 * @author APXEOLOG (Artyom Melnikov), at 17.03.2019
 */

@Service
class CustomUserDetaileService(val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        if (username != null) {
            val user = userRepository.findUserByUsername(username)
            if (user == null) {
                throw IllegalArgumentException("User '$user' not found")
            }
            return CustomPrincipal(user)
        }
        throw IllegalArgumentException("Username cannot be null")
    }
}

data class CustomPrincipal(val user: User) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return user.roles
    }

    override fun isEnabled(): Boolean {
        return user.enabled
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }
}

enum class SecurityRole : GrantedAuthority {
    DEFAULT {
        override fun getAuthority(): String {
            return this.name
        }
    }, USER {
        override fun getAuthority(): String {
            return this.name
        }
    }, ADMIN {
        override fun getAuthority(): String {
            return this.name
        }
    }
}