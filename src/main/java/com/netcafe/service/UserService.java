package com.netcafe.service;

import com.netcafe.dao.UserDAO;
import com.netcafe.model.User;

import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() throws Exception {
        return userDAO.findAll();
    }

    public void updateUser(User user) throws Exception {
        userDAO.update(user);
    }

    public void createUser(String username, String password, String fullName, User.Role role) throws Exception {
        // Hash password
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hash);
        user.setFullName(fullName);
        user.setRole(role);
        user.setTier(User.Tier.BRONZE); // Default tier for new users

        try (java.sql.Connection conn = com.netcafe.util.DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create User
                userDAO.create(conn, user);

                // 2. Create initial account
                com.netcafe.model.Account account = new com.netcafe.model.Account();
                account.setUserId(user.getId());
                account.setBalance(0);
                new com.netcafe.dao.AccountDAO().create(conn, account);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteUser(int userId) throws Exception {
        userDAO.delete(userId);
    }
}
