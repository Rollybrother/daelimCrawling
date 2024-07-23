package com.daelim.crawling.mainProgram.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
	
    @Autowired
    private UserRepository userRepository;
    
    public UserVO save(UserVO user, PasswordEncoder passwordEncoder) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserVO> optionalUser = userRepository.findById(username);
        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        UserVO user = optionalUser.get();
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId())
                .password(user.getPassword())
                .roles(user.getRole() == 1 ? "ADMIN" : "USER")
                .build();
    }
    
    public boolean isIdTaken(String id) {
    	Optional<UserVO> result = userRepository.findById(id);
    	if(result.equals(Optional.empty())) {
    		return false;
    	}else {
    		return true;
    	}
    }
    
}
