package com.playtika.gymsessions.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playtika.gymsessions.dto.SessionDTO;
import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.services.FilterService;
import com.playtika.gymsessions.services.GymSessionQueriesService;
import com.playtika.gymsessions.services.GymSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class GymSessionController {

    @Autowired
    GymSessionService gymSessionService;

    @Autowired
    GymSessionQueriesService queriesService;

    @Autowired
    FilterService filterService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<GymSession> getAll(Pageable pageable) {
        return queriesService.getAll(pageable).toList();
    }

    @PostMapping(value = "/new", params = {"gymName"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GymSession startSession(@RequestParam String gymName) throws SSLException, JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.startGym(gymName, auth.getName());
    }

    @GetMapping("/end")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GymSession endSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.endGym(auth.getName());
    }

    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getCurrentSessionDuration() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.currentDuration(auth.getName());
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getTodayGymtime() throws AuthenticationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.getTodayGymtime(auth.getName());
    }

    @PostMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getGymtimeByDay(@RequestBody String date) throws ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.getTotalGymtimeByDay(auth.getName(), date);
    }

    @PostMapping("/custom-start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GymSession startCustomSession(@RequestBody SessionDTO sessionDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gymSessionService.customStartGym(sessionDTO, auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public GymSession getGymSessionById(@PathVariable long id) {
        return queriesService.getById(id);
    }

    @GetMapping("/alphabetical")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GymSession> getSessionsSortedAlphabeticallyByGymName(Pageable pageable) {
        return queriesService.getSessionsSortedAlphabeticallyByGymName(pageable);
    }

    @GetMapping(params = {"durationHigherThan"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GymSession> getSessionsWithDurationHigherThanValue(int durationHigherThan, Pageable pageable) {
        return queriesService.getSessionsWithDurationHigherThanValue(durationHigherThan, pageable);
    }

    @GetMapping("/started")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GymSession> getSessionsSortedByStartDate(Pageable pageable) {
        return queriesService.getSessionsSortedByStartDate(pageable);
    }

    @GetMapping("/earliest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public GymSession getEarliestSession() {
        return queriesService.getEarliestSession();
    }

    @GetMapping(value = "/custom", params = {"query"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GymSession> getListFromSqlQuery(@RequestParam String query) {
        return filterService.getListFromFilterQuery(query);
    }
}
