package org.techm.samples.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.techm.samples.entity.User;
import org.techm.samples.exception.DuplicateUserException;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.service.auth.UserInfoService;


public class UserInfoServiceTest {

    @Mock 
    private UserInfoRepository repo;
    @Mock 
    private PasswordEncoder encoder;
    @InjectMocks 
    private UserInfoService service;
    
    @BeforeEach
    void init() {
      MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameFound() {
        User u = new User();
        u.setEmail("a@b.com");
        u.setPassword("pass");
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("a@b.com");

        assertEquals("a@b.com", details.getUsername());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(repo.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername("x@x.com"));
    }

    @Test
    void testAddUserSuccess() {
        User u = new User();
        u.setEmail("c@d.com");
        u.setPassword("raw");
        when(repo.findByEmail("c@d.com")).thenReturn(Optional.empty());
        when(encoder.encode("raw")).thenReturn("encoded");
        when(repo.save(u)).thenReturn(u);

        String msg = service.addUser(u);

        assertEquals("User added successfully!", msg);
        assertEquals("encoded", u.getPassword());
    }

    @Test
    void testAddUserDuplicate() {
        User u = new User();
        u.setEmail("dup@c.com");
        when(repo.findByEmail("dup@c.com")).thenReturn(Optional.of(u));

        assertThrows(DuplicateUserException.class, () -> service.addUser(u));
    }
}
