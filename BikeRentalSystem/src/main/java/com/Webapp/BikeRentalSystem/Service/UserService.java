package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.DTO.LoginDTO;
import com.Webapp.BikeRentalSystem.DTO.RegisterDTO;
import com.Webapp.BikeRentalSystem.DTO.UserUpdateDTO;
import com.Webapp.BikeRentalSystem.Filehandling.UserFileHandler;
import com.Webapp.BikeRentalSystem.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserFileHandler fileHandler;

    public UserService(UserFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    // Register: unique email, auto-generate userId, save
    public synchronized User registerUser(RegisterDTO dto) {
        List<User> users = fileHandler.loadUsers();

        boolean exists = users.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(dto.getEmail()));
        if (exists) throw new IllegalArgumentException("Email already registered");

        String newId = fileHandler.nextId(users);
        User u = new User(newId, dto.getName(), dto.getEmail(), dto.getPassword(),
                dto.getPhone(), dto.getAddress(), "Customer");
        users.add(u);
        fileHandler.saveUsers(users);
        return u;
    }

    // Login: email + password must match
    public User loginUser(LoginDTO dto) {
        return fileHandler.loadUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(dto.getEmail())
                        && u.getPassword().equals(dto.getPassword()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }

    // Update: own account only, or Admin for anyone
    public synchronized User updateUser(String id, UserUpdateDTO dto, User loggedIn) {
        if (!loggedIn.getUserId().equals(id) && !"Admin".equals(loggedIn.getRole())) {
            throw new SecurityException("You can only update your own account");
        }
        List<User> users = fileHandler.loadUsers();
        User target = users.stream()
                .filter(u -> u.getUserId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.getName() != null && !dto.getName().isBlank())
            target.setName(dto.getName());
        if (dto.getPhone() != null && !dto.getPhone().isBlank())
            target.setPhone(dto.getPhone());
        if (dto.getAddress() != null && !dto.getAddress().isBlank())
            target.setAddress(dto.getAddress());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getNewPassword().length() < 6)
                throw new IllegalArgumentException("Password must be at least 6 characters");
            target.setPassword(dto.getNewPassword());
        }
        fileHandler.saveUsers(users);
        return target;
    }

    // Delete: own account only, or Admin for anyone
    public synchronized void deleteUser(String id, User loggedIn) {
        if (!loggedIn.getUserId().equals(id) && !"Admin".equals(loggedIn.getRole())) {
            throw new SecurityException("You can only delete your own account");
        }
        List<User> users = fileHandler.loadUsers();
        users.removeIf(u -> u.getUserId().equals(id));
        fileHandler.saveUsers(users);
    }

    // Logout: invalidate session
    public void logOutUser(HttpSession session) {
        session.invalidate();
    }

    public User findById(String id) {
        return fileHandler.loadUsers().stream()
                .filter(u -> u.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public String getUserRoleByEmail(String email) {
        return fileHandler.getUserRoleByEmail(email);
    }

    public User getUserById(String id) {
        return fileHandler.findById(id);
    }
    public List<User> getAllDrivers() {

        return fileHandler.getDrivers();
    }
}
