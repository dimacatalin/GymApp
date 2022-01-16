package com.playtika.gymsessions.services;

import com.playtika.gymsessions.models.User;
import com.playtika.gymsessions.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserQueryService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUser(Pageable pageable) {
        return userRepository.findAll(pageable).toList();
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException());
    }

    public List<User> getUsersAboveThresholdDailyTime(int minimumTime, Pageable pageable) {
        return userRepository.findAll(pageable)
                .stream()
                .filter(user -> user.getMaxDailyTime() > minimumTime)
                .collect(Collectors.toList());
    }

    public User getLowestDailyTimeUser() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getMaxDailyTime))
                .findFirst()
                .orElseThrow(() -> new ArrayIndexOutOfBoundsException("No users found"));
    }

    public User getHighestDailyTimeUser() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getMaxDailyTime).reversed())
                .findFirst()
                .orElseThrow(() -> new ArrayIndexOutOfBoundsException("No users found"));
    }

}

