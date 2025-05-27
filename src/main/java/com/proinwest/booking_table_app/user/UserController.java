package com.proinwest.booking_table_app.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        System.out.println("DEBUG: UserController getUser() called with userId: " + userId);
        UserDTO user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping()
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        UserDTO savedUser = userService.registerUser(user);
        return ResponseEntity.created(userService.location(user))
                .body(savedUser);
    }

    @PatchMapping("{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody User user) {
        UserDTO updatedUser = userService.updateUser(userId, user);
        return ResponseEntity.ok().body(updatedUser);
    }

    @PatchMapping("/role/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> role) {
        UserDTO updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok().body(updatedUser);
    }

    @PatchMapping("/deactivate/{userId}")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User with id " + userId + " deactivated successfully."));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        List<UserDTO> foundUsers = userService.searchUsers(query);
        return ResponseEntity.ok(foundUsers);
    }
}