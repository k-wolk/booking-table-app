package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.AlreadyExistsException;
import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
    public static final String WRONG_EMAIL = "Wrong email address.";
    public static final int PHONE_NUMBER_MIN_LENGTH = 7;
    public static final String PHONE_NUMBER_REGEX = "^\\+?[1-9][0-9]{0,2}([- ]?[0-9]{2,4}){2,3}$";
    public static final String PHONE_MESSAGE = "Phone number should contain at least " + PHONE_NUMBER_MIN_LENGTH + " digits. ";
    public static final String INPUT_IS_MISSING = "Input is missing.";
    public static final String VALID_PHONE_NUMBER = "Examples of valid number are: "
            + "123456789, " + "123 456 789, " + "123-456-7890, " + "+48 123 456 789, " + "+123-123-456-7890";

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final ReservationService reservationService;

    public UserService(UserRepository userRepository, UserDTOMapper userDTOMapper, @Lazy ReservationService reservationService) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
        this.reservationService = reservationService;
    }

    public List<UserDTO> getAllUsers() {
        final Iterable<User> allUsers = userRepository.findAll();
        final List<UserDTO> allUsersList = StreamSupport.stream(allUsers.spliterator(), false)
                .map(userDTOMapper)
                .toList();

        if (allUsersList.isEmpty()) throw new NotFoundException("There are no users in database.");

        return allUsersList;
    }

    public UserDTO getUser(Long id) {
        return userRepository.findById(id)
                .map(userDTOMapper)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " was not found."));
    }

    public UserDTO addUser(User user) {
        if (userRepository.existsByLogin(user.getLogin()))
            throw new AlreadyExistsException("Login " + user.getLogin() + " already exists. It should be unique.");

        final User savedUser = userRepository.save(user);
        return userDTOMapper.apply(savedUser);
    }

    public URI location(User user) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
    }

    public UserDTO updateUser(Long id, User user) {
        final User userToUpdate = userRepository.findById(id)
                .map(updatingUser -> updateUser(user, updatingUser))
                .orElseThrow(() -> new NotFoundException("User with id " + id + " was not found."));

        isPhoneNumberValid(userToUpdate.getPhoneNumber());
        isLoginValid(userToUpdate.getLogin(), id);

        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    public UserDTO partiallyUpdateUser(Long id, User user) {
        final User userToUpdate = userRepository.findById(id)
                .map(updatingUser -> partiallyUpdateUser(user, updatingUser))
                .orElseThrow(() -> new NotFoundException("User with id " + id + " was not found."));


        // todo extract Validation.class
        Map<String, String> validationMessages = validateUser(user);

        List<ValidationError> errors = validate(user);

        isLoginValid(userToUpdate.getLogin(), id);
        isPasswordValid(userToUpdate.getPassword());
        isFirstNameValid(userToUpdate.getFirstName());
        isLastNameValid(userToUpdate.getLastName());
        isEmailValid(userToUpdate.getEmail(), id);
        isPhoneNumberValid(userToUpdate.getPhoneNumber());

        final User savedUser = userRepository.save(userToUpdate);
        return userDTOMapper.apply(savedUser);
    }

    private List<ValidationError> validate(User user) {
        return null;
    }

    private Map<String, String> validateUser(User user) {

        return Map.of();
    }

    public void deleteUser(Long id) {
        existsById(id);
        if (!reservationService.findAllByUserId(id).isEmpty())
            throw new InvalidInputException("User with id " + id + " can not be deleted because it has reservation assigned.");
        userRepository.deleteById(id);
    }

    public List<UserDTO> findAllByLogin(String loginFragment) {
        if (loginFragment == null) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByLogin = userRepository.findAllByLoginContainingIgnoreCase(loginFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByLogin.isEmpty()) throw new NotFoundException("There are no users containing login: " + loginFragment);

        return allByLogin;
    }

    public List<UserDTO> findAllByFirstName(String firstNameFragment) {
        if (firstNameFragment == null) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByFirstName = userRepository.findAllByFirstNameContainingIgnoreCase(firstNameFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByFirstName.isEmpty()) throw new NotFoundException("There are no users containing first name: " + firstNameFragment);

        return allByFirstName;
    }

    public List<UserDTO> findAllByLastName(String lastNameFragment) {
        if (lastNameFragment == null) throw new NotFoundException(INPUT_IS_MISSING);

        final List<UserDTO> allByLastName = userRepository.findAllByLastNameContainingIgnoreCase(lastNameFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByLastName.isEmpty()) throw new NotFoundException("There are no users containing last name: " + lastNameFragment);

        return allByLastName;
    }

    public List<UserDTO> findAllByEmail(String emailFragment) {
        if (emailFragment == null) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByEmail = userRepository.findAllByEmailContainingIgnoreCase(emailFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByEmail.isEmpty()) throw new NotFoundException("There are no users containing email: " + emailFragment);

        return allByEmail;
    }

    public List<UserDTO> findAllByPhoneNumber(String phoneNumberFragment) {
        if (phoneNumberFragment.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByPhoneNumber = userRepository.findAllByPhoneNumberContaining(phoneNumberFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByPhoneNumber.isEmpty()) throw new NotFoundException("There are no users containing phone number: " + phoneNumberFragment);

        return allByPhoneNumber;
    }

    public List<UserDTO> findAllByAnyString(String nameFragment) {
        if (nameFragment == null) throw new InvalidInputException(INPUT_IS_MISSING);

        final List<UserDTO> allByAnyString = userRepository
                .findAllByLoginContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(nameFragment, nameFragment, nameFragment, nameFragment)
                .stream()
                .map(userDTOMapper)
                .toList();

        if (allByAnyString.isEmpty()) throw new NotFoundException("There are no users containing login, name or email: " + nameFragment);

        return allByAnyString;
    }

    public void existsById(Long id) {
        if (!userRepository.existsById(id)) throw new NotFoundException("User with id " + id + " was not found!");
    }

    public void isLoginValid(String login, Long id) {
        if (login.isBlank()) throw new InvalidInputException(FIELD_REQUIRED + LOGIN_MESSAGE);
        if (login.length() < LOGIN_MIN_LENGTH) throw new InvalidInputException(LOGIN_MESSAGE);

        if (!userRepository.findLoginById(id).equals(login)) {
            if (userRepository.existsByLogin(login)) {
                throw new InvalidInputException("Login " + login + " already exists. It should be unique.");
            }
        }
    }

    public void isPasswordValid(String password) {
        if (password.isBlank()) throw new InvalidInputException(FIELD_REQUIRED + PASSWORD_MESSAGE);
        if (password.length() < PASSWORD_MIN_LENGTH) throw new InvalidInputException(PASSWORD_MESSAGE);
    }

    public void isFirstNameValid(String firstName) {
        if (firstName.isBlank()) throw new InvalidInputException(FIELD_REQUIRED);
    }

    public void isLastNameValid(String lastName) {
        if (lastName.isBlank()) throw new InvalidInputException(FIELD_REQUIRED);
    }

    public void isEmailValid(String email, Long id) {
        if (email.isBlank()) throw new InvalidInputException(FIELD_REQUIRED + EMAIL_MESSAGE);

        if (!userRepository.findEmailById(id).equals(email)) {
            if (userRepository.existsByEmail(email)) {
                throw new InvalidInputException("Email address " + email + " already exists. It should be unique.");
            }
        }
        if (email.length() > EMAIL_MAX_LENGTH) throw new InvalidInputException(EMAIL_MESSAGE);
        if (!Pattern.matches(UserService.EMAIL_REGEX, email)) throw new InvalidInputException(WRONG_EMAIL);
    }

    private void isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber == null) throw new InvalidInputException(FIELD_REQUIRED + PHONE_MESSAGE);
        if (phoneNumber.length() < PHONE_NUMBER_MIN_LENGTH) throw new InvalidInputException(PHONE_MESSAGE);
        if (!Pattern.matches(PHONE_NUMBER_REGEX, phoneNumber))
            throw new InvalidInputException(PHONE_MESSAGE + VALID_PHONE_NUMBER);
    }

    private static User updateUser(User user, User updatingUser) {
        updatingUser.setLogin(user.getLogin());
        updatingUser.setPassword(user.getPassword());
        updatingUser.setEmail(user.getEmail());
        updatingUser.setFirstName(user.getFirstName());
        updatingUser.setLastName(user.getLastName());
        updatingUser.setPhoneNumber(user.getPhoneNumber());

        return updatingUser;
    }

    private static User partiallyUpdateUser(User user, User updatingUser) {
        if (user.getLogin() != null) updatingUser.setLogin(user.getLogin());
        if (user.getPassword() != null) updatingUser.setPassword(user.getPassword());
        if (user.getEmail() != null) updatingUser.setEmail(user.getEmail());
        if (user.getFirstName() != null) updatingUser.setFirstName(user.getFirstName());
        if (user.getLastName() != null) updatingUser.setLastName(user.getLastName());
        if (user.getPhoneNumber() != null) updatingUser.setPhoneNumber(user.getPhoneNumber());

        return updatingUser;
    }
}
