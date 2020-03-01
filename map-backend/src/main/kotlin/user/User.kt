package com.apxeolog.hnh.mapbackend.user

import javax.persistence.*

/**
 * @author APXEOLOG (Artyom Melnikov), at 17.03.2019
 */

@Entity
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column
    var password: String,

    @ElementCollection(targetClass = SecurityRole::class)
    @CollectionTable(name = "user_role", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role_id")
    var roles: MutableList<SecurityRole>,

    @Column
    var enabled: Boolean,

    @Column
    var token: String
)