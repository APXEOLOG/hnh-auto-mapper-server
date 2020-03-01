package com.apxeolog.hnh.mapbackend.user

import org.springframework.data.repository.CrudRepository

/**
 * @author APXEOLOG (Artyom Melnikov), at 17.03.2019
 */
interface UserRepository : CrudRepository<User, Int> {
    fun findUserByUsername(username: String): User?
    fun findUserByToken(token: String): User?
}