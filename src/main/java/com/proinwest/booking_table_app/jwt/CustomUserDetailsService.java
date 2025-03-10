package com.proinwest.booking_table_app.jwt;

import com.proinwest.booking_table_app.exceptions.DisableException;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found with login: " + login));

        if (!user.isActive()) {
            throw new DisableException("User account is deactivated.");
        }

        return new CustomUserDetails(user);
    }
}
