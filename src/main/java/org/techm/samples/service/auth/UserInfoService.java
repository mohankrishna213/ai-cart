package org.techm.samples.service.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.User;
import org.techm.samples.exception.DuplicateUserException;
import org.techm.samples.repository.UserInfoRepository;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserInfoService(UserInfoRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return repository.findByEmail(username)
            .map(UserInfoDetails::new)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + username));
    }

    public String addUser(User userInfo) {
        Optional<User> existing = repository.findByEmail(userInfo.getEmail());
        if (existing.isPresent()) {
            throw new DuplicateUserException(userInfo.getEmail());
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "User added successfully!";
    }
}
