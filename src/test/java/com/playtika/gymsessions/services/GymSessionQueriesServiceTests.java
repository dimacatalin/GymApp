package com.playtika.gymsessions.services;

import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.repositories.GymSessionRepository;
import com.playtika.gymsessions.models.Role;
import com.playtika.gymsessions.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GymSessionQueriesServiceTests {

    @MockBean
    private GymSessionRepository gymSessionRepository;

    @Autowired
    private GymSessionQueriesService gymSessionQueriesService;

    private User userMockNoData = mock(User.class);

    @Mock
    private Pageable pageable;

    @MockBean
    private EntityManagerService entityManagerService;

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
        int duration = 70;
        long id = 5;

        when(mockedGymSession.getName()).thenReturn("session");
        when(mockedGymSession.getDuration()).thenReturn(duration);
        when(mockedGymSession.getUser()).thenReturn(user);
        when(mockedGymSession.getUser().getId()).thenReturn(id);

        return mockedGymSession;
    }


    @Test
    public void goodFlowGetAllTest() {
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Page<GymSession> pageGymSessionList = new PageImpl<>(gymSessions);

        when(gymSessionRepository.findAll(pageable)).thenReturn(pageGymSessionList);
        assertThat(gymSessionQueriesService.getAll(pageable)).isEqualTo(pageGymSessionList);
    }

    @Test
    public void noDataGetAllTest() {
        when(gymSessionRepository.findAll(pageable)).thenThrow(new RuntimeException());
        assertThatThrownBy(() -> gymSessionQueriesService.getAll(pageable)).isInstanceOf(Exception.class);
    }

    @Test
    public void existentIdGetById() {
        GymSession gymSession = configureGymSessionMock();

        when(gymSessionRepository.findById(gymSession.getUser().getId())).thenReturn(Optional.of(gymSession));
        assertThat(gymSessionQueriesService.getById(gymSession.getUser().getId()).getName())
                .isEqualTo(gymSession.getName());
    }

    @Test
    public void getSessionsSortedAlphabeticallyByGymNameTest() {
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Page<GymSession> pageGymSessionList = new PageImpl<>(gymSessions);

        when(gymSessionRepository.findAll(pageable)).thenReturn(pageGymSessionList);

        assertThat(gymSessionQueriesService.getSessionsSortedAlphabeticallyByGymName(pageable)).isEqualTo(gymSessions);
    }

    @Test
    public void getSessionsWithDurationHigherThanValueTest() {
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Page<GymSession> pageGymSessionList = new PageImpl<>(gymSessions);

        when(gymSessionRepository.findAll(pageable)).thenReturn(pageGymSessionList);
        when(gymSession.getDuration()).thenReturn(10);

        assertThat(gymSessionQueriesService.getSessionsWithDurationHigherThanValue(5, pageable)).isEqualTo(gymSessions);

    }

    @Test
    public void getSessionsSortedByStartDate() {
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Page<GymSession> pageGymSessionList = new PageImpl<>(gymSessions);
        Date date = new Date();

        when(gymSessionRepository.findAll(pageable)).thenReturn(pageGymSessionList);
        when(gymSession.getStartDate()).thenReturn(date);

        assertThat(gymSessionQueriesService.getSessionsSortedByStartDate(pageable)).isEqualTo(gymSessions);

    }

    @Test
    public void getEarliestSessionTest() {
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        Date date = new Date();

        when(gymSessionRepository.findAll()).thenReturn(gymSessions);
        when(gymSession.getEndDate()).thenReturn(date);

        assertThat(gymSessionQueriesService.getEarliestSession()).isEqualTo(gymSession);

    }

    @Test
    public void getListFromFilterQueryTest() {
        String query = "test";
        GymSession gymSession = configureGymSessionMock();
        List<GymSession> gymSessions = new ArrayList<>();
        gymSessions.add(gymSession);
        String returnQuery = "SELECT * FROM gym_sessions WHERE " + query;

        when(entityManagerService.executeSqlCommand(returnQuery))
                .thenReturn(gymSessions);

        assertThat(gymSessionQueriesService.getListFromFilterQuery(query)).isEqualTo(gymSessions);
    }
}
