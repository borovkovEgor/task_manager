package com.borovkov.srv.service;

import com.borovkov.srv.dto.request.UserRequestDto;
import com.borovkov.srv.dto.response.JwtAuthenticationResponse;
import com.borovkov.srv.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    // Регистрация пользователя
    @Transactional
    public JwtAuthenticationResponse signUp(UserRequestDto request) {

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        try {
            User registeredUser = userService.registerNewUser(user);
            var jwt = jwtService.generatedToken(registeredUser);

            if (jwt == null) {
                throw new RuntimeException("Failed to generate JWT token");
            }
            return new JwtAuthenticationResponse(jwt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    // Аутентификация пользователя
    public JwtAuthenticationResponse signIn(UserRequestDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userDetailsService.loadUserByUsername(request.getUsername());

        var jwt = jwtService.generatedToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
