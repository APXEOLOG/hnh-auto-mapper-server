package com.apxeolog.hnh.mapbackend.user

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author APXEOLOG (Artyom Melnikov), at 17.03.2019
 */

@RestController
@RequestMapping("api/users")
class UserController {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    fun listUsers(): ResponseEntity<String> {
        return ResponseEntity.ok("K")
    }
}