package com.playtika.gymsessions.services;

import com.playtika.gymsessions.models.GymSession;
import com.playtika.gymsessions.repositories.GymSessionRepository;
import com.playtika.gymsessions.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class EntityManagerService {

    @Autowired
    GymSessionRepository gymSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterService filterService;

    @PersistenceContext
    private EntityManager em;

    public List<GymSession> executeSqlCommand(String sqlQuery) {

        return em.createNativeQuery(sqlQuery).getResultList();
    }
}
