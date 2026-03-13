package ca.hanson.shiftflow_backend.config;

import ca.hanson.shiftflow_backend.security.JwtAuthenticationFilter;
import ca.hanson.shiftflow_backend.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
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


                        .requestMatchers(HttpMethod.POST, "/api/shifts").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/shifts/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/shifts/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/shifts/*").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.POST, "/api/swap-requests").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/swap-requests/inbox").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/swap-requests/sent").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/swap-requests/*/accept").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/swap-requests/*/decline").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/open-shifts").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/open-shifts").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/open-shifts/me").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/open-shifts/*/cancel").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/open-shifts/*/claim").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.PUT, "/api/password/change").authenticated()


                        .anyRequest().denyAll()
                )

                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);





        return http.build();
    }
}
