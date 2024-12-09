package com.proinwest.booking_table_app.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping()
    public ResponseEntity<UserDTO> addUser(@RequestBody User user) {
        UserDTO savedUser = userService.addUser(user);
        return ResponseEntity.created(userService.location(user))
                .body(savedUser);
    }

    @PutMapping("{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        UserDTO updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok().body(updatedUser);
    }

    @PatchMapping("{id}")
    public ResponseEntity<UserDTO> partiallyUpdateUser(@PathVariable Long id, @RequestBody User user) {
        UserDTO partiallyUpdatedUser = userService.partiallyUpdateUser(id, user);
        return ResponseEntity.ok().body(partiallyUpdatedUser);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
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

    @GetMapping("/search/{name}")
    public ResponseEntity<List<UserDTO>> findAllUsersByAnyString(@PathVariable String name) {
        List<UserDTO> allUsersByAnyString = userService.findAllByAnyString(name);
        return ResponseEntity.ok(allUsersByAnyString);
    }
}

