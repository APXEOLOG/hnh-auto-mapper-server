package com.apxeolog.hnh.mapbackend

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.firewall.DefaultHttpFirewall
import org.springframework.security.web.firewall.HttpFirewall
import java.io.File

/**
 * @author APXEOLOG (Artyom Melnikov), at 21.01.2019
 */

@EnableScheduling
@SpringBootApplication
class Application

val rootDir: File = File(System.getProperty("user.dir"))
val mapdataFolder: File = rootDir.resolve("public/grids")

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class CustomWebSecurityConfigurerAdapter : WebSecurityConfigurerAdapter() {

    @Value("\${map.user.login}")
    private val mapUserLogin: String? = null

    @Value("\${map.user.password}")
    private val mapUserPassword: String? = null

    @Bean
    fun defaultHttpFirewall(): HttpFirewall {
        return DefaultHttpFirewall()
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser(User
                .withDefaultPasswordEncoder()
                .username(mapUserLogin)
                .password(mapUserPassword)
                .roles("USER"))
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/api/v1/characters", "/api/v1/markers").authenticated()
            .anyRequest().permitAll()
            .and()
            .httpBasic()
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**")
        web.ignoring().antMatchers("/api/v2/updateGrid", "/api/v2/updateCharacter",
            "/api/v1/locate", "/api/v1/uploadMarkers", "/api/v1/removeGrid",
            "/api/v1/setZeroGrid", "/api/v1/cleanup")
    }
}

fun main(args: Array<String>) {
    if (!mapdataFolder.exists()) {
        mapdataFolder.mkdirs()
    }
    SpringApplication.run(Application::class.java, *args)
}
