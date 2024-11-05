package com.proinwest.booking_table_app.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<UserDTO> addUser(@Valid @RequestBody User user) {
            UserDTO savedUser = userService.addUser(user);
            return ResponseEntity.created(userService.location(user)).body(savedUser);
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
    ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/login/{login}")
    public
    ResponseEntity<List<UserDTO>> findAllUsersByLogin(@PathVariable String login) {
        List<UserDTO> allUsersByLogin = userService.findAllByLogin(login);
        return ResponseEntity.ok(allUsersByLogin);
    }

    @GetMapping("/search/firstname/{firstName}")
    public ResponseEntity<List<UserDTO>> findAllUsersByFirstName(@PathVariable String firstName) {
        List<UserDTO> allUsersByName = userService.findAllByFirstName(firstName);
        return ResponseEntity.ok(allUsersByName);
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
        List<UserDTO> findAllUsersByAnyString = userService.findAllByAnyString(name);
        return ResponseEntity.ok(findAllUsersByAnyString);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError) error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}

