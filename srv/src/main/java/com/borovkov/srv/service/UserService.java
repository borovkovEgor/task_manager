package com.borovkov.srv.service;

import com.borovkov.srv.exception.UsernameAlreadyExistsException;
import com.borovkov.srv.models.User;
import com.borovkov.srv.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username={ %s } not found".formatted(username)));
    }

    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id={ %d } not found", id)));
    }

    public void checkExistsUserById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(String.format("User with id={ %d } not found", id));
        }
    }

    public List<Long> getIdsByUsernameIgnoreCaseContaining(String username) {
        return repository.findByUsernameIgnoreCaseContaining(username);
    }

    public List<User> getUsersCreatedByAdmin(Principal principal) {
        User admin = getUserByUsername(principal.getName());
        return repository.findUsersByAdminId(admin.getId());
    }

    public User registerNewUser(User user) {
        checkUsernameExists(user);
        user.setId(null);
        user.setRole(User.Role.ROLE_ADMIN);
        return repository.save(user);
    }

    @Transactional
    public User createNewUser(User user, Principal principal) {
        checkUsernameExists(user);
        User admin = getUserByUsername(principal.getName());

        if (!User.Role.ROLE_ADMIN.equals(admin.getRole())) {
            throw new AccessDeniedException("Only an administrator can create new users");
        }

        user.setCreatedBy(admin.getId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.ROLE_USER);

        return repository.save(user);
    }

    @Transactional
    public User updateUser(User newUser, Principal principal) {

        User oldUser = getUserByUsername(principal.getName());

        if (newUser.getUsername() != null) {
            checkUsernameExists(newUser.getUsername());
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getPassword() != null) {
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        return repository.save(oldUser);
    }

    @Transactional
    public List<Long> deleteUsers(List<Long> ids, Principal principal) {
        User admin = getUserByUsername(principal.getName());
        if (!User.Role.ROLE_ADMIN.equals(admin.getRole())) {
            throw new AccessDeniedException("Only an administrator can delete users");
        }
        checkUsersCreatedByAdmin(admin.getId(), ids);

        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                deletedIds.add(id);
            } else {
                throw new EntityNotFoundException(String.format("User with id={ %d } not found", id));
            }
        }

        return deletedIds;
    }

    private void checkUsersCreatedByAdmin(Long adminId, List<Long> userIds) {
        userIds.forEach(userId -> {
            User user = repository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(String.format("User with id={ %d } not found", userId)));
            if (!adminId.equals(user.getCreatedBy())) {
                throw new AccessDeniedException("You are not allowed to manage users created by another administrator");
            }
        });
    }

    public void checkUsernameExists(User user) {
        if (repository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(String.format("User with username={ %s } is already exist", user.getUsername()));
        }
    }

    public void checkUsernameExists(String username) {
        if (repository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException(String.format("User with username={ %s } is already exist", username));
        }
    }

    public Long getGroupId(User user) {
        return user.getCreatedBy() != null ? user.getCreatedBy() : user.getId();
    }

    public Long getGroupId(String username) {
        User user = getUserByUsername(username);
        return getGroupId(user);
    }
}
