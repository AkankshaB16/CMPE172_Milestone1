package edu.sjsu.cmpe172.careconnect.service;

import edu.sjsu.cmpe172.careconnect.model.UserAccount;
import edu.sjsu.cmpe172.careconnect.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

  

    public UserAccount authenticate(
            String email,
            String password
    ) {
        return userRepository.findByEmail(email)
                .filter(user ->
                        passwordEncoder.matches(
                                password,
                                user.passwordHash()
                        )
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );
    }
}