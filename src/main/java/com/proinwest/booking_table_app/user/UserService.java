package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.types.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class UserService {
    public static final String ACCESS_DENIED = "Access denied.";
    public static final int EMAIL_MAX_LENGTH = 70;
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String EMAIL_MESSAGE = "Email address should contain max " + EMAIL_MAX_LENGTH + " characters.";
    public static final String WRONG_EMAIL = "Wrong email address format.";
    public static final String FIELD_REQUIRED = "This field is required. ";
    public static final int LOGIN_MIN_LENGTH = 3;
    public static final String LOGIN_MESSAGE = "Login should contain at least " + LOGIN_MIN_LENGTH + " characters.";
    public static final String NO_USERS_IN_DATABASE = "There are no users in database.";
    public static final int PASSWORD_MIN_LENGTH = 12;
    public static final String PASSWORD_MESSAGE = "Password should contain at least " + PASSWORD_MIN_LENGTH + " characters.";
    public static final int PHONE_NUMBER_MIN_LENGTH = 7;
    public static final String PHONE_NUMBER_REGEX = "^\\+?[1-9][0-9]{0,2}([- ]?[0-9]{2,4}){2,3}$";
    public static final String PHONE_MESSAGE = "Phone number should contain at least " + PHONE_NUMBER_MIN_LENGTH + " digits. ";
    public static final String USER_ID_IS_REQUIRED = "User id is required.";
    public static final String VALID_PHONE_NUMBER = "Examples of valid number are: "
            + "123456789, " + "123 456 789, " + "123-456-7890, " + "+48 123 456 789, " + "+123-123-456-7890";

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final ReservationService reservationService;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserDTOMapper userDTOMapper,
                       @Lazy ReservationService reservationService,
                       UserValidator userValidator,
                       PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
        this.reservationService = reservationService;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    List<UserDTO> getAllUsers() {
        final Iterable<User> allUsers = userRepository.findAll();
        final List<UserDTO> allUsersDTOList = StreamSupport.stream(allUsers.spliterator(), false)
                .map(userDTOMapper)
                .toList();

        if (allUsersDTOList.isEmpty()) throw new NotFoundException(NO_USERS_IN_DATABASE);

        return allUsersDTOList;
    }

    UserDTO getUser(Long userId) {
        isAdminOrOwner(userId);

        return userRepository.findById(userId)
                .map(userDTOMapper)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));
    }

    UserDTO registerUser(User user) {
        validateNewUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        final User savedUser = userRepository.save(user);
        return userDTOMapper.apply(savedUser);
    }

    URI location(User user) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
    }

    UserDTO updateUser(Long userId, User user) {
        isCurrentUser(userId);

        final User userToUpdate = userRepository.findById(userId)
                .map(updatingUser -> updateUser(user, updatingUser))
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));

        validateUserToUpdate(user, userId);

        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    UserDTO updateUserRole(Long userId, Map<String, String> role) {
        final User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));

        String newRole = role.get("role");
        if (newRole == null || newRole.isBlank())   throw new InvalidInputException("Role cannot be empty.");
        if (!isValidRole(newRole))                  throw new InvalidInputException("Invalid role.");

        userToUpdate.setRole(newRole);
        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    void deactivateUser(Long userId) {
        isAdminOrOwner(userId);

        final User userToDeactivate = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));

        userToDeactivate.setActive(false);
        userRepository.save(userToDeactivate);
    }

    List<UserDTO> searchUsers(String query) {
        if (query.isBlank()) throw new InvalidInputException(ReservationService.INPUT_IS_MISSING);

        String[] terms = query.split("\\s+");
        Set<UserDTO> resultSet = new HashSet<>();

        for (String term : terms) {
            List<UserDTO> users = userRepository.searchUsers(term)
                    .stream()
                    .map(userDTOMapper)
                    .toList();

            resultSet.addAll(users);
        }

        if (resultSet.isEmpty()) throw new NotFoundException("There are no users containing: " + query);

        return new ArrayList<>(resultSet);
    }

    String findLoginByUserId(Long userId) {
        return userRepository.findLoginByUserId(userId);
    }

    String findEmailByUserId(Long userId) {
        return userRepository.findEmailByUserId(userId);
    }

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateUserToUpdate(User userToUpdate, Long userId) {
        Map<String, String> validationMessages = userValidator.validateUserToUpdate(userToUpdate, userId);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    private void validateNewUser(User userToUpdate) {
        Map<String, String> validationMessages = userValidator.validateNewUser(userToUpdate);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    private boolean isValidRole(String role) {
        return role.equals("USER") || role.equals("ADMIN");
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    public void isAdminOrOwner(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !userDetails.getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_DENIED);
    }

    void isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!userDetails.getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_DENIED);
    }

    private static User updateUser(User user, User updatingUser) {
        if (user.getLogin() != null)        updatingUser.setLogin(user.getLogin());
        if (user.getPassword() != null)     updatingUser.setPassword(user.getPassword());
        if (user.getEmail() != null)        updatingUser.setEmail(user.getEmail());
        if (user.getFirstName() != null)    updatingUser.setFirstName(user.getFirstName());
        if (user.getLastName() != null)     updatingUser.setLastName(user.getLastName());
        if (user.getPhoneNumber() != null)  updatingUser.setPhoneNumber(user.getPhoneNumber());

        return updatingUser;
    }
}