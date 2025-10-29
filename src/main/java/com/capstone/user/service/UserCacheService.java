package com.capstone.user.service;

import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCacheService {

    private final UserRepository userRepository;

    @Cacheable(value = "user", key = "'username:' + #username")
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "user", key = "'email:' + #email")
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "user", key = "'phone:' + #phone")
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Cacheable(value = "user", key = "'id:' + #userId")
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public boolean isExistsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
