package com.example.userserver.service;

import com.example.userserver.model.User;
import com.example.userserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");

        when(userRepository.save(user)).thenReturn(user);

        Mono<User> result = userService.createUser(user);

        StepVerifier.create(result)
                .expectNextMatches(createdUser -> createdUser.getUsername().equals("testuser"))
                .verifyComplete();
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Mono<User> result = userService.getUserById(1L);

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getUsername().equals("testuser"))
                .verifyComplete();
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        Flux<User> result = userService.getAllUsers();

        StepVerifier.create(result)
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();
    }

    // @Test
    // public void testUpdateUser() {
    //     User user = new User();
    //     user.setId(1L);
    //     user.setUsername("updatedUser");
    //     user.setPassword("updatedPassword");

    //     when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    //     when(userRepository.save(user)).thenReturn(user);

    //     Mono<User> result = userService.updateUser(1L, user);

    //     StepVerifier.create(result)
    //             .expectNextMatches(updatedUser -> updatedUser.getUsername().equals("updatedUser") &&
    //                     updatedUser.getPassword().equals("updatedPassword"))
    //             .verifyComplete();
    // }

    // @Test
    // public void testDeleteUser() {
    //     User user = new User();
    //     user.setId(1L);
    //     user.setUsername("testuser");

    //     when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    //     doNothing().when(userRepository).delete(user);

    //     Mono<Void> result = userService.deleteUser(1L);

    //     StepVerifier.create(result)
    //             .verifyComplete();

    //     verify(userRepository, times(1)).delete(user);
    // }
}
