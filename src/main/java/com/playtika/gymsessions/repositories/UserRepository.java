package com.playtika.gymsessions.repositories;

import com.playtika.gymsessions.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByUsername(String username);

    @Transactional
    void deleteByUsername(String username);

    @Override
    void delete(User user);
}
