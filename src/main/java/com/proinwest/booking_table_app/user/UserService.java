package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
public class UserService {
    public static final String FIELD_REQUIRED = "This field is required. ";
    public static final int LOGIN_MIN_LENGTH = 3;
    public static final String LOGIN_MESSAGE = "Login should contain at least " + LOGIN_MIN_LENGTH + " characters.";
    public static final int PASSWORD_MIN_LENGTH = 12;
    public static final String PASSWORD_MESSAGE = "Password should contain at least " + PASSWORD_MIN_LENGTH + " characters.";
    public static final int EMAIL_MAX_LENGTH = 70;
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String EMAIL_MESSAGE = "Email address should contain max " + EMAIL_MAX_LENGTH + " characters.";
    public static final String WRONG_EMAIL = "Wrong email address format.";
    public static final int PHONE_NUMBER_MIN_LENGTH = 7;
    public static final String PHONE_NUMBER_REGEX = "^\\+?[1-9][0-9]{0,2}([- ]?[0-9]{2,4}){2,3}$";
    public static final String PHONE_MESSAGE = "Phone number should contain at least " + PHONE_NUMBER_MIN_LENGTH + " digits. ";
    public static final String INPUT_IS_MISSING = "Input is missing.";
    public static final String VALID_PHONE_NUMBER = "Examples of valid number are: "
            + "123456789, " + "123 456 789, " + "123-456-7890, " + "+48 123 456 789, " + "+123-123-456-7890";
    public static final String NO_USERS_IN_DATABASE = "There are no users in database.";
    public static final String USER_ID_IS_REQUIRED = "User id is required.";

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final ReservationService reservationService;
    private final UserValidator userValidator;

    public UserService(UserRepository userRepository, UserDTOMapper userDTOMapper, @Lazy ReservationService reservationService, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
        this.reservationService = reservationService;
        this.userValidator = userValidator;
    }

    List<UserDTO> getAllUsers() {
        final Iterable<User> allUsers = userRepository.findAll();
        final List<UserDTO> allUsersList = StreamSupport.stream(allUsers.spliterator(), false)
                .map(userDTOMapper)
                .toList();

        if (allUsersList.isEmpty()) throw new NotFoundException(NO_USERS_IN_DATABASE);

        return allUsersList;
    }

    UserDTO getUser(Long userId) {
        return userRepository.findById(userId)
                .map(userDTOMapper)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));
    }

    UserDTO addUser(User user) {
        validateUser(user);

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
        final User userToUpdate = userRepository.findById(userId)
                .map(updatingUser -> updateUser(user, updatingUser))
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));

        validateUser(user, userId);

        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    UserDTO partiallyUpdateUser(Long userId, User user) {
        final User userToUpdate = userRepository.findById(userId)
                .map(updatingUser -> partiallyUpdateUser(user, updatingUser))
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found."));

        validatePartialUser(user, userId);

        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    void deleteUser(Long userId) {
        if(!existsById(userId)) throw new NotFoundException("User with id " + userId + " was not found.");

        if (!reservationService.findAllByUserId(userId).isEmpty())
            throw new InvalidInputException("User with id " + userId + " can not be deleted because he has at least one reservation assigned.");

        userRepository.deleteById(userId);
    }

    List<UserDTO> findAllByLogin(String loginFragment) {
        if (loginFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByLogin = userRepository.findAllByLogin(loginFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByLogin.isEmpty()) throw new NotFoundException("There are no users containing login: " + loginFragment);

        return allByLogin;
    }

    List<UserDTO> findAllByFirstName(String firstNameFragment) {
        if (firstNameFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByFirstName = userRepository.findAllByFirstName(firstNameFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByFirstName.isEmpty()) throw new NotFoundException("There are no users containing first name: " + firstNameFragment);

        return allByFirstName;
    }

    List<UserDTO> findAllByLastName(String lastNameFragment) {
        if (lastNameFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByLastName = userRepository.findAllByLastName(lastNameFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByLastName.isEmpty()) throw new NotFoundException("There are no users containing last name: " + lastNameFragment);

        return allByLastName;
    }

    List<UserDTO> findAllByEmail(String emailFragment) {
        if (emailFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByEmail = userRepository.findAllByEmail(emailFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByEmail.isEmpty()) throw new NotFoundException("There are no users containing email: " + emailFragment);

        return allByEmail;
    }

    List<UserDTO> findAllByPhoneNumber(String phoneNumberFragment) {
        if (phoneNumberFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByPhoneNumber = userRepository.findAllByPhoneNumber(phoneNumberFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByPhoneNumber.isEmpty()) throw new NotFoundException("There are no users containing phone number: " + phoneNumberFragment);

        return allByPhoneNumber;
    }

    List<UserDTO> findAllByAnyStringField(String searchingPhrase) {
        if (searchingPhrase.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByAnyString = userRepository
                .findAllByAnyString(searchingPhrase)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByAnyString.isEmpty()) throw new NotFoundException("There are no users containing login, name, email or phone number: " + searchingPhrase);

        return allByAnyString;
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

    private void validateUser(User userToUpdate, Long userId) {
        Map<String, String> validationMessages = userValidator.validateUser(userToUpdate, userId);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    private void validatePartialUser(User userToUpdate, Long userId) {
        Map<String, String> validationMessages = userValidator.validatePartialUser(userToUpdate, userId);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    void validateUser(User userToUpdate) {
        Map<String, String> validationMessages = userValidator.validateUser(userToUpdate);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    static User updateUser(User user, User updatingUser) {
        updatingUser.setLogin(user.getLogin());
        updatingUser.setPassword(user.getPassword());
        updatingUser.setEmail(user.getEmail());
        updatingUser.setFirstName(user.getFirstName());
        updatingUser.setLastName(user.getLastName());
        updatingUser.setPhoneNumber(user.getPhoneNumber());

        return updatingUser;
    }

    static User partiallyUpdateUser(User user, User updatingUser) {
        if (user.getLogin() != null) updatingUser.setLogin(user.getLogin());
        if (user.getPassword() != null) updatingUser.setPassword(user.getPassword());
        if (user.getEmail() != null) updatingUser.setEmail(user.getEmail());
        if (user.getFirstName() != null) updatingUser.setFirstName(user.getFirstName());
        if (user.getLastName() != null) updatingUser.setLastName(user.getLastName());
        if (user.getPhoneNumber() != null) updatingUser.setPhoneNumber(user.getPhoneNumber());

        return updatingUser;
    }
}