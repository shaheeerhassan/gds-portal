package com.school.service.impl;

import com.school.dao.impl.*;
import com.school.dao.interfaces.*;
import com.school.exceptions.*;
import com.school.model.*;
import com.school.service.interfaces.AuthenticationService;
import com.school.utils.PasswordEncryption;
import com.school.utils.TokenGenerator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.school.validations.ValidatorUtil.*;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final AdministratorDao administratorDao;
    private final TeacherDao teacherDao;
    private final PrincipalDao principalDao;
    private final StudentDao studentDao;
    private final ParentDao parentDao;
    private final PasswordResetTokenDao passwordResetTokenDao;

    private static final long RESET_TOKEN_VALID_HOURS = 24;

    public AuthenticationServiceImpl() {
        userDao = new UserDaoImpl();
        roleDao = new RoleDaoImpl();
        administratorDao = new AdministratorDaoImpl();
        teacherDao = new TeacherDaoImpl();
        principalDao = new PrincipalDaoImpl();
        studentDao = new StudentDaoImpl();
        parentDao = new ParentDaoImpl();
        passwordResetTokenDao = new PasswordResetTokenDaoImpl();
    }

    @Override
    public User login(String email, String password) {
        email = validateEmail(email);
        validatePassword(password);

        User user = userDao.getUserByEmail(email);
        if (user == null)
            throw new UnauthorizedException("Invalid email or password.");

        if (!user.isActive())
            throw new AccountDisableException("Account is disabled. Contact the administrator.");

        if (!PasswordEncryption.matches(password, user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password.");

        userDao.updateLastLogin(user.getUserId());
        return user;
    }

    @Override
    public void changePassword(long userId, String currentPassword, String newPassword) {
        validateId(userId);
        validatePassword(currentPassword);
        validatePassword(newPassword);

        User user = userDao.getUserById(userId);
        if (user == null)
            throw new ResourceNotFoundException("User not found.");

        if (!PasswordEncryption.matches(currentPassword, user.getPasswordHash()))
            throw new UnauthorizedException("Current password is incorrect.");

        userDao.updatePasswordHash(userId, PasswordEncryption.encode(newPassword));
    }

    @Override
    public String requestPasswordReset(String email) {
        email = validateEmail(email);

        User user = userDao.getUserByEmail(email);
        if (user == null)
            return null;

        String rawToken = TokenGenerator.generateToken();
        String tokenHash = TokenGenerator.sha256(rawToken);

        passwordResetTokenDao.invalidateUserTokens(user.getUserId());

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getUserId());
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(RESET_TOKEN_VALID_HOURS));
        resetToken.setUsed(false);

        if (!passwordResetTokenDao.insertToken(resetToken))
            throw new BusinessRuleException("Could not create a reset token.");

        return rawToken;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        validateRequired(token, "Reset token");
        validatePassword(newPassword);

        String tokenHash = TokenGenerator.sha256(token);
        PasswordResetToken resetToken = passwordResetTokenDao.getToken(tokenHash);
        if (resetToken == null || resetToken.isUsed())
            throw new UnauthorizedException("Invalid or already used reset token.");
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new UnauthorizedException("Reset token has expired.");

        userDao.updatePasswordHash(resetToken.getUserId(), PasswordEncryption.encode(newPassword));
        passwordResetTokenDao.markTokenAsUsed(tokenHash);
        passwordResetTokenDao.invalidateUserTokens(resetToken.getUserId());
    }

    @Override
    public Map<String, Object> getUserProfile(long userId) {
        validateId(userId);

        User user = userDao.getUserById(userId);
        if (user == null)
            throw new ResourceNotFoundException("User not found.");

        Role role = roleDao.getRoleById(user.getRoleId());
        if (role == null)
            throw new ResourceNotFoundException("Role not found for user.");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("user", user);
        profile.put("role", role);

        switch (role.getRoleName()) {
            case "ADMINISTRATOR":
                profile.put("profile", administratorDao.getAdminByUserId(userId));
                break;
            case "PRINCIPAL":
                profile.put("profile", principalDao.getPrincipalByUserId(userId));
                break;
            case "TEACHER":
                profile.put("profile", teacherDao.getTeacherByUserId(userId));
                break;
            case "STUDENT":
                profile.put("profile", studentDao.getStudentByUserId(userId));
                break;
            case "PARENT":
                profile.put("profile", parentDao.getParentByUserId(userId));
                break;
            default:
                profile.put("profile", null);
        }
        return profile;
    }
}
