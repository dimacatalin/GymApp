package com.playtika.gymsessions.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playtika.gymsessions.exceptions.OverAllocatedTimeException;
import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.repositories.GymSessionRepository;
import com.playtika.gymsessions.models.Role;
import com.playtika.gymsessions.models.User;
import com.playtika.gymsessions.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
import java.text.ParseException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GymSessionServiceTests {

    @Autowired
    private GymSessionService gymSessionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GymSessionRepository gymSessionRepository;

    private GymSession gymSessioNoData = mock(GymSession.class);

    @Mock
    private Pageable pageable;

    public User configureUserMock() {
        User mockedUser = mock(User.class);
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);

        when(mockedUser.getUsername()).thenReturn("user");
        when(mockedUser.getPassword()).thenReturn("pwd");
        when(mockedUser.getId()).thenReturn(1L);
        when(mockedUser.getRoles()).thenReturn(roles);


        return mockedUser;
    }

    private GymSession configureGymSessionMock() {
        User user = configureUserMock();
        GymSession mockedGymSession = mock(GymSession.class);

        when(mockedGymSession.getName()).thenReturn("session");
        when(mockedGymSession.getDuration()).thenReturn(70);
        when(mockedGymSession.getUser()).thenReturn(user);


        return mockedGymSession;
    }

    @Test
    public void goodFlowStartGymTest() throws SSLException, JsonProcessingException {
        User user = configureUserMock();
        GymSession gymSession = configureGymSessionMock();
        Date date = new Date();
        List<GymSession> gymSessions = new ArrayList<>();
        Optional<Integer> duration = Optional.of(5);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);
        when(gymSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(duration);
        when(user.getMaxDailyTime()).thenReturn(200);
        when(gymSessionRepository.saveAndFlush(any())).thenReturn(gymSession);
        assertThat(gymSessionService.startGym(gymSession.getName(), user.getUsername())
                .getName())
                .isEqualTo(gymSession.getName());
    }

    @Test
    public void gymNullStartGymTest() {
        User user = configureUserMock();
        List<GymSession> gymSessions = new ArrayList<>();

        GymSession gymSession = configureGymSessionMock();
        gymSessions.add(gymSession);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);

        assertThatThrownBy(() -> gymSessionService.startGym(gymSession.getName(), user.getUsername()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void overTimeStartGymTest() {
        User user = configureUserMock();
        List<GymSession> gymSessions = new ArrayList<>();
        Date date = new Date();
        Optional<Integer> duration = Optional.of(50);

        GymSession gymSession = configureGymSessionMock();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);
        when(gymSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(duration);
        when(user.getMaxDailyTime()).thenReturn(-5);
        when(gymSessionRepository.saveAndFlush(any(GymSession.class))).thenReturn(gymSession);

        assertThatThrownBy(() -> gymSessionService.startGym(gymSession.getName(), user.getUsername()))
                .isInstanceOf(OverAllocatedTimeException.class);
    }

    @Test
    public void goodFlowEndGymTest() {
        User user = configureUserMock();
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Date date = new Date();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);
        when(gymSession.getStartDate()).thenReturn(date);
        when(gymSessionRepository.saveAndFlush(any(GymSession.class))).thenReturn(gymSession);
        assertThat(gymSessionService.endGym(user.getUsername())
                .getName())
                .isEqualTo(gymSession.getName());
    }

    @Test
    public void goodFlowTwoSessionsEndGymTest() {
        User user = configureUserMock();
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
        Date date = cal.getTime();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);
        when(gymSession.getStartDate()).thenReturn(date);
        when(gymSessionRepository.saveAndFlush(any(GymSession.class))).thenReturn(gymSession);
        assertThat(gymSessionService.endGym(user.getUsername())
                .getName())
                .isEqualTo(gymSession.getName());
    }

    @Test
    public void goodFlowCurrentDurationTest() {
        User user = configureUserMock();
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Date date = new Date();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSession.getStartDate()).thenReturn(date);
        when(gymSessionRepository.getOngoingGymSessions(user.getId())).thenReturn(gymSessions);
        when(gymSessionRepository.saveAndFlush(any(GymSession.class))).thenReturn(gymSession);

        assertThat(gymSessionService.currentDuration(user.getUsername())).isNotNull();
    }

    @Test
    public void usernameNullGetTodayGymtime() throws ParseException {

        assertThatThrownBy(() -> gymSessionService.getTodayGymtime(null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void goodFlowGetTodayGymtime() throws AuthenticationException {
        User user = configureUserMock();
        Date date = new Date();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gymSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(Optional.of(5));

        assertThat(gymSessionService.getTodayGymtime(user.getUsername()))
                .isNotNull();
    }
}
