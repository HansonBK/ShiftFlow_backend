package ca.hanson.shiftflow_backend.config;

import ca.hanson.shiftflow_backend.security.JwtAuthenticationFilter;
import ca.hanson.shiftflow_backend.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtTokenProvider jwtTokenProvider;
    private final String allowedOrigins;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          @Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())



                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll()

                        .requestMatchers(HttpMethod.GET,"/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/schedule").authenticated()

                        .requestMatchers(HttpMethod.GET,"/api/admin/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole("ADMIN")


                        .requestMatchers(HttpMethod.GET, "/api/shifts/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shifts/employee/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/shifts/all").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/shifts").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/shifts/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/shifts/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/shifts/*").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.POST, "/api/swap-requests").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/swap-requests/inbox").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/swap-requests/sent").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/swap-requests/*/accept").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/swap-requests/*/decline").hasRole("EMPLOYEE")


                        .anyRequest().denyAll()
                )

                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);





        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
