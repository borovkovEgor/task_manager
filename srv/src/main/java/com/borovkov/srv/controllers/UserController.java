package com.borovkov.srv.controllers;

import com.borovkov.srv.dto.request.UserRequestDto;
import com.borovkov.srv.dto.response.UserResponseDto;
import com.borovkov.srv.mapper.CommonMapper;
import com.borovkov.srv.models.User;
import com.borovkov.srv.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommonMapper commonMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение списка пользователей, созданных админом")
    @GetMapping("/get/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserResponseDto>> getUserCreatedByAdmin(Principal principal) {
        List<User> users = userService.getUsersCreatedByAdmin(principal);
        List<UserResponseDto> dtoList = users.stream()
                .map(commonMapper::toUserResponseDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @Operation(summary = "Получение информации о пользователе для личного кабинета")
    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserResponseDto> getUser(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        return new ResponseEntity<>(commonMapper.toUserResponseDto(user), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание нового пользователя. Доступно пользователю с ролью - ADMIN")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto request, Principal principal) {
        User user = commonMapper.toUserJpa(request);
        UserResponseDto response = commonMapper.toUserResponseDto(userService.createNewUser(user, principal));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Смена пароля и имени пользователя")
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserRequestDto request, Principal principal) {
        User updatedUser = commonMapper.toUserJpa(request);
        UserResponseDto response = commonMapper.toUserResponseDto(userService.updateUser(updatedUser, principal));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удаление пользователя. Доступно пользователю с ролью - ADMIN")
    @DeleteMapping("/{ids}/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Long>> deleteUsers(@PathVariable List<Long> ids, Principal principal) {
        List<Long> deletedIds = userService.deleteUsers(ids, principal);
        return ResponseEntity.ok(deletedIds);
    }

}
