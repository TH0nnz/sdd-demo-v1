package com.example.timesheet.security;

import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * Loads user-specific data by username (email) for authentication.
 * The User entity implements UserDetails interface, so it can be returned directly.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Load user by username (email address).
     * 
     * @param username the user's email address
     * @return UserDetails implementation (User entity)
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + username
                ));
        
        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                "User account is disabled: " + username
            );
        }
        
        return user;
    }
    
    /**
     * Load user by ID (used for token-based authentication).
     *
     * @param userId the user's ID
     * @return UserDetails implementation (User entity)
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with id: " + userId
                ));
        
        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                "User account is disabled with id: " + userId
            );
        }
        
        return user;
    }
}
