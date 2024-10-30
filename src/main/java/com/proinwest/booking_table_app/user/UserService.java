package com.proinwest.booking_table_app.user;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;

    public UserService(UserRepository userRepository, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
    }

    public List<UserDTO> getAllUsers() {
        Iterable<User> allUsers = userRepository.findAll();
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUser(Long id) {
        return userRepository.findById(id)
                .map(userDTOMapper);
    }

    public UserDTO addUser(User user) {
        User savedUser = userRepository.save(user);
        return userDTOMapper.apply(savedUser);
    }

    public URI location(User user) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
    }

    public UserDTO updateUser(Long id, User user) {
        userRepository.findById(id)
                .map(updatingUser -> {
                    updatingUser.setLogin(user.getLogin());
                    updatingUser.setPassword(user.getPassword());
                    updatingUser.setEmail(user.getEmail());
                    updatingUser.setFirstName(user.getFirstName());
                    updatingUser.setLastName(user.getLastName());
                    updatingUser.setPhoneNumber(user.getPhoneNumber());

                    return userRepository.save(updatingUser);
                });

        return userDTOMapper.apply(user);
    }

    public UserDTO partiallyUpdateUser(Long id, User user) {
        userRepository.findById(id)
                .map(updatingUser -> {
                    if (user.getLogin() != null) updatingUser.setLogin(user.getLogin());
                    if (user.getPassword() != null) updatingUser.setPassword(user.getPassword());
                    if (user.getEmail() != null) updatingUser.setEmail(user.getEmail());
                    if (user.getFirstName() != null) updatingUser.setFirstName(user.getFirstName());
                    if (user.getLastName() != null) updatingUser.setLastName(user.getLastName());
                    if (user.getPhoneNumber() != null) updatingUser.setPhoneNumber(user.getPhoneNumber());

                    return userRepository.save(updatingUser);
                });

        return userDTOMapper.apply(user);
    }

    public boolean existsUserById(Long id) {
        return userRepository.existsById(id);
    }
    public void deleteUser(Long id) {userRepository.deleteById(id);}

    public List<UserDTO> findAllByLogin(String loginFragment) {
        return userRepository.findAllByLoginContainingIgnoreCase(loginFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllByFirstName(String firstNameFragment) {
        return userRepository.findAllByFirstNameContainingIgnoreCase(firstNameFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllByLastName(String lastNameFragment) {
        return userRepository.findAllByLastNameContainingIgnoreCase(lastNameFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllByEmail(String emailFragment) {
        return userRepository.findAllByEmailContainingIgnoreCase(emailFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllByPhoneNumber(String phoneNumberFragment) {
        return userRepository.findAllByPhoneNumberContaining(phoneNumberFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllByAnyString(String nameFragment) {
        return userRepository
                .findAllByLoginContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(nameFragment, nameFragment, nameFragment, nameFragment)
                .stream()
                .map(userDTOMapper)
                .collect(Collectors.toList());
    }
}
