package com.netcafe.service;

import com.netcafe.dao.UserDAO;
import com.netcafe.model.User;
import com.netcafe.util.PasswordUtil;

import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String password) throws Exception {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found");
        }
        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new Exception("Invalid password");
        }
        return user;
    }
}
