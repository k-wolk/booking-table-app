package com.proinwest.booking_table_app.security.userDetails;

import com.proinwest.booking_table_app.exceptions.types.DisableException;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("DEBUG: CustomUserDetailsService initialized!");
    }

//    @Override
//    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
//        User user = userRepository.findByLogin(login)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
//
//        System.out.println("DEBUG: Loaded user from DB: " + user.getLogin());
//        System.out.println("DEBUG: User class: " + user.getClass().getName());
//
//        if (!user.isActive()) {
//            throw new DisableException("User account is deactivated.");
//        }
//
//        CustomUserDetails customUserDetails = new CustomUserDetails(user);
//        System.out.println("DEBUG: Returning CustomUserDetails: " + customUserDetails.getClass().getName());
//        return customUserDetails;
//    }


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        System.out.println("DEBUG11: loadUserByUsername() called with login: " + login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));

        System.out.println("DEBUG: Loaded user from DB: " + user.getLogin());
        System.out.println("DEBUG: User class: " + user.getClass().getName());



        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        System.out.println("DEBUG: Returning CustomUserDetails: " + customUserDetails.getClass().getName());

        return customUserDetails;
    }


    // todo Prawidłe poniżej?

//    @Override
//    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
//        User user = userRepository.findByLogin(login)
//                .orElseThrow(() -> new UsernameNotFoundException("User was not found with login: " + login));
//
//        if (!user.isActive()) {
//            throw new DisableException("User account is deactivated.");
//        }
//
//        return new CustomUserDetails(user);
//    }
}
