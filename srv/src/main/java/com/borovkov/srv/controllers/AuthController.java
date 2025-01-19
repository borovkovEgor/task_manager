package com.borovkov.srv.controllers;

import com.borovkov.srv.dto.request.UserRequestDto;
import com.borovkov.srv.dto.response.JwtAuthenticationResponse;
import com.borovkov.srv.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public JwtAuthenticationResponse register(@RequestBody UserRequestDto request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public JwtAuthenticationResponse login(@RequestBody UserRequestDto request) {
        return authenticationService.signIn(request);
    }

}
