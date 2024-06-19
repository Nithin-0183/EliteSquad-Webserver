package com.example.userserver.service;

import com.example.userserver.model.User;
import com.example.userserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SecurityServiceTest {

    @InjectMocks
    private SecurityService securityService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser() {
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String[] roles = {"ROLE_USER"};

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(roles));

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Mono<User> result = Mono.just(securityService.registerUser(username, password, roles));

        StepVerifier.create(result)
                .expectNextMatches(registeredUser -> registeredUser.getUsername().equals(username) &&
                        registeredUser.getPassword().equals(encodedPassword) &&
                        registeredUser.getRoles().contains("ROLE_USER"))
                .verifyComplete();

        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testFindByUsername() {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mono<User> result = Mono.justOrEmpty(securityService.findByUsername(username));

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getUsername().equals(username))
                .verifyComplete();

        verify(userRepository, times(1)).findByUsername(username);
    }

    // @Test
    // public void testUpdateUserRoles() {
    //     String username = "testuser";
    //     String[] newRoles = {"ROLE_ADMIN"};

    //     User user = new User();
    //     user.setUsername(username);
    //     user.setRoles(Set.of("ROLE_USER"));

    //     when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    //     when(userRepository.save(user)).thenReturn(user);

    //     Mono<User> result = securityService.updateUserRoles(username, newRoles);

    //     StepVerifier.create(result)
    //             .expectNextMatches(updatedUser -> updatedUser.getRoles().contains("ROLE_ADMIN") &&
    //                     !updatedUser.getRoles().contains("ROLE_USER"))
    //             .verifyComplete();

    //     verify(userRepository, times(1)).findByUsername(username);
    //     verify(userRepository, times(1)).save(user);
    // }

    // @Test
    // public void testDeleteUser() {
    //     String username = "testuser";

    //     User user = new User();
    //     user.setUsername(username);

    //     when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    //     doNothing().when(userRepository).delete(user);

    //     Mono<Void> result = securityService.deleteUser(username);

    //     StepVerifier.create(result)
    //             .verifyComplete();

    //     verify(userRepository, times(1)).findByUsername(username);
    //     verify(userRepository, times(1)).delete(user);
    // }
}
