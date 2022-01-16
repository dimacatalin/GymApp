package com.playtika.gymsessions.services;

import com.playtika.gymsessions.models.FilterInterface;
import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.repositories.GymSessionRepository;
import com.playtika.gymsessions.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GymSessionQueriesService implements FilterInterface {

    @Autowired
    GymSessionRepository gymSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterService filterService;

    @Autowired
    private EntityManagerService entityManagerService;

    public Page<GymSession> getAll(Pageable pageable) {
        return gymSessionRepository.findAll(pageable);
    }

    public GymSession getById(long id) {
        return gymSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid id"));
    }

    public List<GymSession> getSessionsSortedAlphabeticallyByGymName(Pageable pageable) {
        return gymSessionRepository.findAll(pageable)
                .stream()
                .sorted(Comparator.comparing(GymSession::getName))
                .collect(Collectors.toList());
    }

    public List<GymSession> getSessionsWithDurationHigherThanValue(int value, Pageable pageable) {
        return gymSessionRepository.findAll(pageable)
                .stream()
                .filter(session -> session.getDuration() > value)
                .collect(Collectors.toList());
    }

    public List<GymSession> getSessionsSortedByStartDate(Pageable pageable) {
        return gymSessionRepository.findAll(pageable)
                .stream()
                .sorted(Comparator.comparing(GymSession::getStartDate))
                .collect(Collectors.toList());
    }

    public GymSession getEarliestSession() {
        return gymSessionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GymSession::getEndDate))
                .findFirst().orElseThrow(() -> new ArrayIndexOutOfBoundsException("No sessions found"));
    }


    @Override
    public List<GymSession> getListFromFilterQuery(String query) {
        String sqlQuery;
        sqlQuery = "SELECT * FROM gym_sessions WHERE " + query;
        return entityManagerService.executeSqlCommand(sqlQuery);
    }
}
