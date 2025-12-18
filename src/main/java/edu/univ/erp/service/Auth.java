package edu.univ.erp.service;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;

public class Auth {

    private static final String URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String USER = "root";
    private static final String PASSWORD = "harsh@102";
    private static final String host = "localhost";
    private static final String dbName = "auth_db";
    private static final String sqlFilePath = "src/main/java/edu/univ/erp/backup/auth_backup.sql";
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_SECONDS = 60;

    private static final java.util.Map<String, Integer> failedAttempts = new java.util.HashMap<>();
    private static final java.util.Map<String, Instant> lockouts = new java.util.HashMap<>();

    // ✅ Hash password using bcrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static String getrole(String username) throws Exception {
        String role = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "SELECT role FROM users_auth WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            } else {
                throw new Exception("User not found");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return role;
    }

    public static boolean restore() throws Exception {
            try {
            System.out.println("Starting restore...");

            ProcessBuilder pb = new ProcessBuilder(
                    "mysql",
                    "-h", host,
                    "-u", USER,
                    "-p" + PASSWORD
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // send SQL file to mysql
            Files.copy(Paths.get(sqlFilePath), process.getOutputStream());
            process.getOutputStream().close();

            int status = process.waitFor();
            

            if (status == 0) {
                System.out.println("Restore completed successfully!");
            } else {
                System.out.println("Restore FAILED! Exit code: " + status);
            }

        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }

    public static boolean backupDatabase() throws Exception {

        try {
            System.out.println("Starting backup...");

            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-h", host,
                    "-u", USER,
                    "-p" + PASSWORD,
                    "--databases", dbName
            );

            pb.redirectErrorStream(false);


            pb.redirectOutput(new File(sqlFilePath));

            Process process = pb.start();


            InputStream err = process.getErrorStream();
            new Thread(() -> {
                try {
                    err.transferTo(System.out);
                } catch (Exception ignored) {}
            }).start();

            int code = process.waitFor();

            return code == 0;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }



    // ✅ Insert user
    public static boolean insertUser(String username, String role, String plainPassword) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String hash = hashPassword(plainPassword);

            String sql = "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setString(3, hash);

            int rows = pstmt.executeUpdate();
            System.out.println(rows + " user(s) inserted.");
            return rows > 0;
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            throw new Exception(e.getMessage() );

        }
        
    }

    public static boolean login(String username, String plainPassword) throws Exception {
        
        // --- 1. CHECK LOCKOUT STATUS FIRST ---
        if (lockouts.containsKey(username)) {
            Instant unlockTime = lockouts.get(username);
            if (Instant.now().isBefore(unlockTime)) {
                // User is still locked out
                long secondsLeft = java.time.Duration.between(Instant.now(), unlockTime).getSeconds();
                throw new Exception("Account locked. Try again in " + secondsLeft + " seconds.");
            } else {
                // Lockout expired, clear it and allow attempt
                lockouts.remove(username);
                failedAttempts.remove(username);
            }
        }

        boolean success = false;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "SELECT password_hash FROM users_auth WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    // --- SUCCESS ---
                    success = true;
                    
                    // Reset failures for this user
                    failedAttempts.remove(username);
                    lockouts.remove(username);
                    
                    System.out.println("✅ Login successful for " + username);

                    // Update last login
                    String updateSql = "UPDATE users_auth SET last_login = NOW() WHERE username = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, username);
                    updateStmt.executeUpdate();
                } else {
                    // --- FAILURE ---
                    // Increment count for THIS SPECIFIC user
                    int attempts = failedAttempts.getOrDefault(username, 0) + 1;
                    failedAttempts.put(username, attempts);

                    if (attempts >= MAX_ATTEMPTS) {
                        // Trigger 60 second lockout
                        lockouts.put(username, Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS));
                        throw new Exception("Wrong Password entered 5 times. Account locked for 60 seconds.");
                    } else {
                        throw new Exception("Incorrect password. Attempt " + attempts + "/" + MAX_ATTEMPTS);
                    }
                }
            } else {
                throw new Exception("User not found");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return success;
    }

    public static boolean verifyPassword(String plainPassword, String storedHash)  {
        return BCrypt.checkpw(plainPassword, storedHash);
    }
    public static boolean changeUserStatus(String username, String newStatus) throws Exception {
        String sql = "UPDATE users_auth SET status = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, username);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ User status updated successfully for: " + username);
                return true;
            } else {
                System.out.println("⚠️ No user found with username: " + username);
                throw new Exception("User not found");
            }

        } catch (Exception e) {
            
            throw new Exception(e.getMessage());
        }
    }

    // ✅ Delete a user by username
   public static boolean deleteUser(String username) throws Exception {
    String sql = "DELETE FROM users_auth WHERE username = ?";

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        Class.forName("com.mysql.cj.jdbc.Driver");

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, username);

        int rows = pstmt.executeUpdate();
        if (rows > 0) {
            System.out.println("🗑️ User deleted successfully: " + username);
            return true;
        } else {
            System.out.println("⚠️ No user found with username: " + username);
            throw new Exception("User not found");
        }
    }
    catch ( ClassNotFoundException e) {
        throw new Exception("Problem with connecting to DB");
    }
     catch(Exception e) {
        throw new Exception(e.getMessage());
    }

    
}


public static ArrayList<ArrayList<String>>  view_admins() throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String sql3 =  "SELECT user_id, username, role " + 
                "FROM auth_db.users_auth " + 
                "WHERE role = 'admin'" ;

        PreparedStatement stmt3 = conn.prepareStatement(sql3);
        ResultSet rs = stmt3.executeQuery();

        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();

            row.add(String.valueOf(rs.getInt("user_id")));
            row.add(rs.getString("username"));
            row.add(rs.getString("role"));
            row.add(rs.getString("role"));

            table.add(row);
        }

    } catch (Exception e) {
        throw new Exception(e.getMessage());    
    }

    return table;
}


    // ✅ Change password securely
    public static boolean changePassword(String username, String oldPlainPassword, String newPlainPassword) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String checkSql = "SELECT password_hash FROM users_auth WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (BCrypt.checkpw(oldPlainPassword, storedHash)) {
                    String newHash = hashPassword(newPlainPassword);

                    String updateSql = """
                        UPDATE users_auth
                        SET old_password_hash = password_hash,
                            password_hash = ?,
                            password_changed_at = NOW()
                        WHERE username = ?;
                    """;

                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, newHash);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();

                    System.out.println("✅ Password updated successfully!");
                    return true;
                } else {

                    System.out.println("❌ Incorrect current password!");
                    throw new Exception("Incorrect current password");
                }
            } else {
                System.out.println("❌ User not found!");
                throw new Exception("User not found");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    

}
