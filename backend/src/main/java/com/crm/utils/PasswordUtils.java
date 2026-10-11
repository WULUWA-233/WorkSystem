package com.crm.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Password Utility - BCrypt Encryption
 *
 * @author CRM Team
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * Encode raw password with BCrypt
     *
     * @param rawPassword raw password
     * @return encoded password
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * Verify raw password against encoded password
     *
     * @param rawPassword     raw password
     * @param encodedPassword encoded password
     * @return true if matched
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * Utility main method to generate BCrypt password hashes for data.sql
     *
     * Usage: Run this main method and copy the output to data.sql
     */
    public static void main(String[] args) {
        String[] passwords = {"123456"};
        for (String pwd : passwords) {
            System.out.println("Password: " + pwd + " -> " + encode(pwd));
        }
    }
}
