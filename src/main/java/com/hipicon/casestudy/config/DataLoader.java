package com.hipicon.casestudy.config;

import com.hipicon.casestudy.entity.User;
import com.hipicon.casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("hipicon")) {
            User user = new User();
            user.setUsername("hipicon");
            user.setPassword(passwordEncoder.encode("password"));
            user.setEmail("hipicon@example.com");
            userRepository.save(user);
        }
    }
}
