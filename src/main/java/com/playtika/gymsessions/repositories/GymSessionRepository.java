package com.playtika.gymsessions.repositories;

import com.playtika.gymsessions.models.GymSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymSessionRepository extends JpaRepository<GymSession, Long> {

    @Query(value = "SELECT * FROM gym_sessions g WHERE g.end_date IS NULL AND g.user_id = :idUser", nativeQuery = true)
    List<GymSession> getOngoingGymSessions(long idUser);

    @Query(value = "SELECT SUM(duration) FROM gym_sessions g WHERE g.user_id = :idUser AND DATE(start_date) = DATE(:date)", nativeQuery = true)
    Optional<Integer> getDurationOnDay(long idUser, Date date);

}
