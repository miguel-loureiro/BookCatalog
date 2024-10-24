package com.bookcatalog.backend.service;

import com.bookcatalog.backend.model.CustomUserDetails;
import com.bookcatalog.backend.model.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bookcatalog.backend.model.User;
import com.bookcatalog.backend.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("guestuser".equals(username)) {

            User guestUser = new User();
            guestUser.setUsername("guestuser");
            guestUser.setRole(Role.GUEST);
            return new CustomUserDetails(guestUser);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + username));
        return new CustomUserDetails(user);
    }
}
