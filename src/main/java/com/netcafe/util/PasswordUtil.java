package com.netcafe.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    // Main method to generate hashes for manual DB insertion
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Hash for '" + args[0] + "': " + hashPassword(args[0]));
        } else {
            System.out.println("Usage: java com.netcafe.util.PasswordUtil <password_to_hash>");
            System.out.println("Example hash for 'admin123': " + hashPassword("admin123"));
        }
    }
}
