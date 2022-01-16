package com.playtika.gymsessions.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playtika.gymsessions.dto.SessionDTO;
import com.playtika.gymsessions.exceptions.OverAllocatedTimeException;
import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.repositories.GymSessionRepository;
import com.playtika.gymsessions.models.User;
import com.playtika.gymsessions.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class GymSessionService {

    @Autowired
    GymSessionRepository gymSessionRepository;

    @Autowired
    UserRepository userRepository;


    public GymSession startGym(String gymName, String username) throws SSLException, JsonProcessingException {
        GymSession gymSession = new GymSession();
        User currentUser = userRepository.findByUsername(username);
        Date startDate = new Date();
        int minutesAlreadyPlayedToday = 0;
        gymName = checkedGym(gymName);

        if (gymName == null) {
            throw new IllegalArgumentException();
        }
        if (!gymSessionRepository.getOngoingGymSessions(currentUser.getId()).isEmpty()) {
            throw new IllegalStateException();
        }
        if (gymSessionRepository.getDurationOnDay(currentUser.getId(), startDate).isPresent()) {
            minutesAlreadyPlayedToday = gymSessionRepository.getDurationOnDay(currentUser.getId(), startDate).get();
        }
        gymSession = initialSetup(gymSession, gymName, startDate, currentUser);
        if (minutesAlreadyPlayedToday > currentUser.getMaxDailyTime()) {
            gymSessionRepository.saveAndFlush(gymSession);
            throw new OverAllocatedTimeException();
        }
        return gymSessionRepository.saveAndFlush(gymSession);
    }


    public GymSession customStartGym(SessionDTO sessionDTO, String username) {
        GymSession gymSession = new GymSession();
        User currentUser = userRepository.findByUsername(username);

        if (!gymSessionRepository.getOngoingGymSessions(currentUser.getId()).isEmpty()) {
            throw new IllegalStateException();
        }

        gymSession.setName(sessionDTO.getName());
        gymSession.setStartDate(getYesterdayDate());
        gymSession.setUser(currentUser);
        return gymSessionRepository.saveAndFlush(gymSession);
    }

    public GymSession endGym(String username) {

        User currentUser = userRepository.findByUsername(username);
        long userId = currentUser.getId();
        boolean gymSessionsEmpty = gymSessionRepository.getOngoingGymSessions(userId).isEmpty();
        boolean multipleGymSessions = gymSessionRepository.getOngoingGymSessions(userId).size() >= 2;

        if (gymSessionsEmpty || multipleGymSessions) {
            throw new IllegalStateException();
        } else {
            GymSession gymSession = gymSessionRepository.getOngoingGymSessions(userId).get(0);
            Date endDate = new Date();
            if (isOnSameDay(gymSession.getStartDate(), endDate)) {
                return finishNewGymSession(gymSession, endDate);
            } else {
                setSecondGymSession(gymSession, endDate);
                Date firstSessionEndDate = setEndOfDay(gymSession.getStartDate());
                return finishNewGymSession(gymSession, firstSessionEndDate);
            }
        }
    }

    public String currentDuration(String username) {

        User currentUser = userRepository.findByUsername(username);
        long userId = currentUser.getId();
        boolean gymSessionsEmpty = gymSessionRepository.getOngoingGymSessions(userId).isEmpty();
        boolean multipleGymSessions = gymSessionRepository.getOngoingGymSessions(userId).size() >= 2;

        if (gymSessionsEmpty || multipleGymSessions) {
            return null;
        } else {
            GymSession gymSession = gymSessionRepository.getOngoingGymSessions(userId).get(0);
            Date currentEndDate = new Date();
            int currentSessionDuration = (int) getDateDiff(gymSession.getStartDate(), currentEndDate, TimeUnit.MINUTES);
            return String.format("Current session duration: %d minutes", currentSessionDuration);
        }

    }

    public String getTotalGymtimeByDay(String username, String dateToParse) throws ParseException {
        User currentUser = userRepository.findByUsername(username);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateConverted = dateToParse + " " + "00:00:00";
        Date date = df.parse(dateConverted);
        Optional<Integer> time = gymSessionRepository.getDurationOnDay(currentUser.getId(), date);
        if (time.isPresent()) {
            return String.format("You have played %d minutes on %s", time, date);
        }
        return String.format("You have not played any gyms on %s", date);
    }


    public String getTodayGymtime(String username) throws AuthenticationException {
        if (!checkAuthentication(username)) {
            throw new AuthenticationException();
        }

        User currentUser = userRepository.findByUsername(username);
        Date date = new Date();
        Optional<Integer> time = gymSessionRepository.getDurationOnDay(currentUser.getId(), date);
        if (time.isPresent()) {
            String response = "Today you have exercised for " + time + " minutes";
            return response;
        } else {
            String response = "Today you have not worked out";
            return response;
        }
    }

    private long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private boolean checkAuthentication(String username) {
        if (username == null || username.isEmpty())
            return false;
        return true;
    }

    private boolean isOnSameDay(Date startDate, Date endDate) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal2.setTime(endDate);

        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }

    private GymSession finishNewGymSession(GymSession gymSession, Date endDate) {
        gymSession.setEndDate(endDate);
        int duration = (int) getDateDiff(gymSession.getStartDate(), endDate, TimeUnit.MINUTES);
        gymSession.setDuration(duration);
        return gymSessionRepository.saveAndFlush(gymSession);
    }

    private void setSecondGymSession(GymSession gymSession, Date endDate) {

        Date startOfDayOfSecondSession = resetDay(endDate);
        int duration = (int) getDateDiff(startOfDayOfSecondSession, endDate, TimeUnit.MINUTES);

        GymSession newGymSession = new GymSession();
        newGymSession.setUser(gymSession.getUser());
        newGymSession.setName(gymSession.getName());
        newGymSession.setStartDate(startOfDayOfSecondSession);
        newGymSession.setEndDate(endDate);
        newGymSession.setDuration(duration);
        gymSessionRepository.saveAndFlush(newGymSession);
    }

    private Date resetDay(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        return cal1.getTime();
    }

    private Date setEndOfDay(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 23);
        cal1.set(Calendar.MINUTE, 59);
        cal1.set(Calendar.SECOND, 59);
        return cal1.getTime();
    }

    private Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
        return cal.getTime();
    }

    private String formatGymName(String gymName) {
        String[] splitedGymName = gymName.split("\\s+");
        String gymNameReformatted = new String();
        for (String word : splitedGymName) {
            gymNameReformatted += "-";
            gymNameReformatted += word;
        }
        gymNameReformatted = gymNameReformatted.substring(1);
        return gymNameReformatted;
    }

    private String checkedGym(String gymName) throws SSLException, JsonProcessingException {

        String gymNameReformatted = formatGymName(gymName);

        return gymNameReformatted;
    }

    private GymSession initialSetup(GymSession gymSession, String gymName, Date startDate, User currentUser) {
        gymSession.setName(gymName);
        gymSession.setStartDate(startDate);
        gymSession.setUser(currentUser);
        return gymSession;
    }
}
