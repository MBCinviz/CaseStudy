package com.hipicon.casestudy.service;

import com.hipicon.casestudy.dto.JwtResponse;
import com.hipicon.casestudy.dto.LoginRequest;
import com.hipicon.casestudy.exception.UnauthorizedException;
import com.hipicon.casestudy.exception.UserNotFoundException;
import com.hipicon.casestudy.repository.UserRepository;
import com.hipicon.casestudy.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            if (jwt == null || jwt.isBlank()) {
                log.warn("Kullanıcıya ait kayıt bulunamadı: " + loginRequest.getUsername());
                throw new UserNotFoundException("Kullanıcıya ait kayıt bulunamadı: " + loginRequest.getUsername());
            }
            return new JwtResponse(jwt, loginRequest.getUsername());
        }
        catch(BadCredentialsException ex){
            log.warn("Kullanıcı adı veya şifre hatalı!");
            throw new UnauthorizedException("Kullanıcı adı veya şifre hatalı!");

        }
    }
}
