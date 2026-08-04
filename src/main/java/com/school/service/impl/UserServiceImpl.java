package com.school.service.impl;

import com.school.dao.impl.UserDaoImpl;
import com.school.dao.interfaces.UserDao;
import com.school.exceptions.DaoException;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.User;
import com.school.service.interfaces.UserService;
import com.school.utils.PasswordEncryption;
import com.school.utils.UsernameGenerator;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl() {
        userDao = new UserDaoImpl();
    }

    @Override
    public User createUser(User user, String password) {
        user.setEmail(validateEmail(user.getEmail()));
        validatePassword(password);
        validateId(user.getRoleId());
        if (user.getUsername() != null)
            user.setUsername(validateUsername(user.getUsername()));

        if (userDao.getUserByEmail(user.getEmail()) != null)
            throw new DuplicateResourceException("A user with this email already exists.");
        if (user.getUsername() != null && userDao.getUserByUsername(user.getUsername()) != null)
            throw new DuplicateResourceException("This username is already taken.");

        user.setPasswordHash(PasswordEncryption.encode(password));
        user.setActive(true);

        int attempts = 0;
        while (attempts < 3) {
            if (user.getUsername() == null) {
                user.setUsername(UsernameGenerator.generateUsername());
            }

            try {
                if (userDao.insertUser(user)) {
                    return user;
                }
            } catch (DaoException e) {
                user.setUsername(null);
                attempts++;
            }
        }

        throw new IllegalStateException("Failed to create user.");
    }

    @Override
    public User getUserById(long userId) {
        validateId(userId);
        User user = userDao.getUserById(userId);
        if (user == null)
            throw new ResourceNotFoundException("User not found.");
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        email = validateEmail(email);
        User user = userDao.getUserByEmail(email);
        if (user == null)
            throw new ResourceNotFoundException("User not found.");
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        username = validateUsername(username);
        User user = userDao.getUserByUsername(username);
        if (user == null)
            throw new ResourceNotFoundException("User not found.");
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public List<User> getUsersByRole(int roleId) {
        validateId(roleId);
        return userDao.getUsersByRole(roleId);
    }

    @Override
    public void updateUser(User user) {
        validateId(user.getUserId());
        validateId(user.getRoleId());
        user.setEmail(validateEmail(user.getEmail()));
        user.setUsername(validateUsername(user.getUsername()));

        if (!userDao.updateUser(user))
            throw new ResourceNotFoundException("User not found.");
    }

    @Override
    public void updateUserStatus(long userId, boolean isActive) {
        validateId(userId);
        if (!userDao.updateUserStatus(userId, isActive))
            throw new ResourceNotFoundException("User not found.");
    }

    @Override
    public void deleteUser(long userId) {
        validateId(userId);
        if (!userDao.deleteUser(userId))
            throw new ResourceNotFoundException("User not found.");
    }

    @Override
    public void updateProfilePicture(long userId, String profilePictureUrl) {
        validateId(userId);
        validateRequired(profilePictureUrl, "Profile picture URL");
        if (!userDao.updateProfilePicture(userId, profilePictureUrl))
            throw new ResourceNotFoundException("User not found.");
    }
}
