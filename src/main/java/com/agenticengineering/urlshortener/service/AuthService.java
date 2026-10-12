
package com.agenticengineering.urlshortener.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agenticengineering.urlshortener.entity.AppUser;
import com.agenticengineering.urlshortener.repository.AppUserRepository;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String username, String password) {

        String normalizedUsername = username.trim();

        if (normalizedUsername.length() < 3
                || normalizedUsername.length() > 50) {
            throw new IllegalArgumentException(
                    "Username must contain 3 to 50 characters");
        }

        if (password.length() < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException(
                    "Username is already registered");
        }

        AppUser user = new AppUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String username, String password) {
        AppUser user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return user;
    }

}
