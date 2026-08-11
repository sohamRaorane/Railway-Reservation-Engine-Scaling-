package com.soham.railway_reservation_engine.security.config;


import com.soham.railway_reservation_engine.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanRegistrarDslMarker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // make it as a  config class --> spring will scan all the beans and register all the bean methods
@RequiredArgsConstructor //automatically injects the  final wale constructor
public class SecurityConfig {
    /*
    what happens if we do not use this class?
        Every endpoint is protected.
        Spring Boot generates a random password.
        It shows a default login page (meant for browser-based apps).
     */
    private final JwtFilter jwtFilter;

    @Bean // bean is simply an object that is instantiated, assembled, and managed by a Spring IoC container.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //An AuthenticationManager is a core component of Spring Security that is responsible for processing authentication requests.
    // It acts as a central point for handling authentication logic in a Spring Security application.
    //So we will require this under the auth service
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception{
        return configuration.getAuthenticationManager();
    }
    //Client --> SecurityFilterChain (Multiple filter or multiple checks) -->  Controller
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Disable CSRF for the rest api's
                //CSRF --> Cross-Site Request Forgery --> it is mainly for browsers sessions that uses the cookies
                // we are using the stateless auth and we will not use the server side sessions --> hence diabling it
                .csrf(csrf -> csrf.disable()) // we will not use the csrf for the webhook)


                // used for  creating the stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                )
                //Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // so this request will not need any auth
                        .requestMatchers("/payment/webhook/**").permitAll()
                        //if my database contains the admin --> so then the spring maps it to ROLE_ADMIN --> AS WE HAVE CREATED IT IN THE CUSTOMER_DETAIL_SERVICE
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN") // only admin can access this request

                        .anyRequest().authenticated() // remaining all the request would be authenticated
                )
                /*
                "Run my custom JwtFilter before Spring Seurity's UsernamePasswordAuthenticationFilter so that JWT authentication happens first.
                 If the JWT is valid, the user is authenticated before the request reaches the rest of the security chain."
                 */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // .httpBasic(Customizer.withDefaults());


        return http.build(); // builds the complete security config in to the security chain filter --> that the spring uses for every
    }

}
/*
Flow of my security config
                            Incoming Request
                                    │
                                    ▼
                            JwtFilter
                                    │
                                    ├── Extract Bearer Token
                                    ├── Validate JWT
                                    ├── Load User
                                    └── Set Authentication
                                    │
                                    ▼
                            Remaining Spring Security Filters
                                    │
                                    ▼
                            Controller
 */
