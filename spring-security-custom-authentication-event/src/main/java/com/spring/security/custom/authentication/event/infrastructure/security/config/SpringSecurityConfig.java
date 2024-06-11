package com.spring.security.custom.authentication.event.infrastructure.security.config;

import com.spring.security.custom.authentication.event.infrastructure.event.custom.CustomAuthenticationSuccessEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SpringSecurityConfig {
    private final ApplicationContext applicationContext;
    private final AuthenticationProvider authenticationProvider;

    public SpringSecurityConfig(ApplicationContext applicationContext,
                                AuthenticationProvider authenticationProvider) {
        this.applicationContext = applicationContext;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user").hasAuthority("ROLE_USER")
                        .requestMatchers("/db").hasAuthority("ROLE_DB")
                        .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")
                        .anyRequest().permitAll());

        http
                .formLogin(form -> form
                        .successHandler((request, response, authentication) -> {
                            applicationContext.publishEvent(new CustomAuthenticationSuccessEvent(authentication));
                            response.sendRedirect("/");
                        }));

        http
                .cors(AbstractHttpConfigurer::disable);

        http
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user").password("{noop}1111").roles("USER").build();
        return new InMemoryUserDetailsManager(user);
    }
}
