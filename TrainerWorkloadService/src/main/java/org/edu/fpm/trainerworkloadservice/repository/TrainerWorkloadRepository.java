package org.edu.fpm.trainerworkloadservice.repository;

import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, Long> {
//    @Query("SELECT tw FROM TrainerWorkload tw WHERE tw.username = :username " +
//            "AND YEAR(tw.date) = :year AND MONTH(tw.date) = :month")
//    Optional<TrainerWorkload> findByUsernameAndYearAndMonth(@Param("username") String username,
//                                                            @Param("year") int year,
//                                                            @Param("month") int month);
    List<TrainerWorkload> findByUsername(String username);
}
