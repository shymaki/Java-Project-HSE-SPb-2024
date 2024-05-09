package com.example.server.service;

import com.example.server.dto.UserLoginDto;
import com.example.server.dto.UserRegistrationDto;
import com.example.server.dto.UserUpdateDto;
import com.example.server.model.FileInfo;
import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserService {

    private static final String DIRECTORY_PATH = "server\\src\\main\\resources\\profile_pic\\";

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        User existingUser = userRepository.findByUsername(registrationDto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username already in use");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(hashPassword(registrationDto.getPassword()));

        return userRepository.save(user);
    }

    public User loginUser(UserLoginDto loginDto) {
        User user = userRepository.findByUsername(loginDto.getUsername());
        if (user != null && user.getPassword().equals(hashPassword(loginDto.getPassword()))) {
            return user;
        }
        throw new RuntimeException("Invalid username or password");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public User updateUser(Integer userId, UserUpdateDto updateDto) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(updateDto.getUsername());
        user.setEmail(updateDto.getEmail());
        user.setPassword(updateDto.getPassword());
        user.setFirstName(updateDto.getFirstName());
        user.setSecondName(updateDto.getSecondName());
        user.setDateOfBirth(updateDto.getDateOfBirth());
        user.setCountry(updateDto.getCountry());
        user.setCity(updateDto.getCity());
        user.setEducation(updateDto.getEducation());
        user.setBio(updateDto.getBio());
        user.setResumeUrl(updateDto.getResumeUrl());

        return userRepository.save(user);
    }

    public User uploadUserProfilePicture(Integer userId, MultipartFile profilePicture) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String extension = FilenameUtils.getExtension(profilePicture.getOriginalFilename());
        String key = FileInfoService.generateKey(profilePicture.getOriginalFilename());
        String newProfilePictureUrl = DIRECTORY_PATH + userId + "\\" + key + "." + extension;
        try {
            FileInfoService.uploadFileData(profilePicture.getBytes(), newProfilePictureUrl);
        } catch (Exception e) {
            throw new IOException("Can't upload profile picture");
        }

        String profilePictureUrl = user.getProfilePictureUrl();
        if (profilePictureUrl != null && !Objects.equals(profilePictureUrl, "")) {
            deleteUserProfilePicture(userId);
        }

        user.setProfilePictureUrl(newProfilePictureUrl);

        return userRepository.save(user);
    }

    public User deleteUserProfilePicture(Integer userId) throws IOException {
        User user = getUser(userId);
        Files.delete(Paths.get(user.getProfilePictureUrl()));
        user.setProfilePictureUrl("");
        return userRepository.save(user);
    }

    public User getUser(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> getUsersList(Set<Integer> userIds) {
        return userRepository.findAllById(userIds);
    }

    public byte[] getUserProfilePicture(Integer userId) throws IOException {
        User user = getUser(userId);
        try {
            if (user.getProfilePictureUrl() == null || Objects.equals(user.getProfilePictureUrl(), "")) {
                return null;
            }
            return Files.readAllBytes((Paths.get(user.getProfilePictureUrl())));
        } catch (IOException e) {
            throw new IOException("Can't download file");
        }
    }
}
