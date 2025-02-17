package com.proinwest.booking_table_app.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping()
    public ResponseEntity<UserDTO> addUser(@RequestBody User user) {
        UserDTO savedUser = userService.addUser(user);
        return ResponseEntity.created(userService.location(user))
                .body(savedUser);
    }

    @PutMapping("{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody User user) {
        UserDTO updatedUser = userService.updateUser(userId, user);
        return ResponseEntity.ok().body(updatedUser);
    }

    @PatchMapping("{userId}")
    public ResponseEntity<UserDTO> partiallyUpdateUser(@PathVariable Long userId, @RequestBody User user) {
        UserDTO partiallyUpdatedUser = userService.partiallyUpdateUser(userId, user);
        return ResponseEntity.ok().body(partiallyUpdatedUser);
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/login/{login}")
    public ResponseEntity<List<UserDTO>> findAllUsersByLogin(@PathVariable String login) {
        List<UserDTO> allUsersByLogin = userService.findAllByLogin(login);
        return ResponseEntity.ok(allUsersByLogin);
    }

    @GetMapping("/search/firstname/{firstName}")
    public ResponseEntity<List<UserDTO>> findAllUsersByFirstName(@PathVariable String firstName) {
        List<UserDTO> allUsersByFirstName = userService.findAllByFirstName(firstName);
        return ResponseEntity.ok(allUsersByFirstName);
    }

    @GetMapping("/search/lastname/{lastName}")
    public ResponseEntity<List<UserDTO>> findAllUsersByLastname(@PathVariable String lastName) {
        List<UserDTO> allUsersByLastName = userService.findAllByLastName(lastName);
        return ResponseEntity.ok(allUsersByLastName);
    }

    @GetMapping("/search/email/{email}")
    public ResponseEntity<List<UserDTO>> findAllUsersByEmail(@PathVariable String email) {
        List<UserDTO> allUsersByEmail = userService.findAllByEmail(email);
        return ResponseEntity.ok(allUsersByEmail);
    }

    @GetMapping("/search/phone/{phoneNumber}")
    public ResponseEntity<List<UserDTO>> findAllUsersByPhoneNumber(@PathVariable String phoneNumber) {
        List<UserDTO> allUsersByPhoneNumber = userService.findAllByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(allUsersByPhoneNumber);
    }

    @GetMapping("/search/{anyString}")
    public ResponseEntity<List<UserDTO>> findAllUsersByAnyStringField(@PathVariable String anyString) {
        List<UserDTO> allUsersByAnyString = userService.findAllByAnyStringField(anyString);
        return ResponseEntity.ok(allUsersByAnyString);
    }
}