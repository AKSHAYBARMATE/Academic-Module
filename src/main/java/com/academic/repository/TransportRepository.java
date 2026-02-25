package com.academic.repository;

import com.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface TransportRepository extends JpaRepository<Student, Integer> {

    @Query(value = "SELECT t.status as trip_status, " +
            "my_stop.stop_name as my_stop_name, " +
            "DATE_FORMAT(my_stop.pickup_time, '%l:%i %p') as my_stop_time, " +
            "next_stop.stop_name as next_stop_heading " +
            "FROM trips t " +
            "JOIN route_stop_students rss ON rss.student_id = :studentId " +
            "JOIN route_stops my_stop ON rss.route_stop_id = my_stop.id " +
            "LEFT JOIN route_stops next_stop ON next_stop.route_id = t.route_id " +
            "AND next_stop.sequence_order = (" +
            "    SELECT COALESCE(MAX(rs_inner.sequence_order), 0) + 1 " +
            "    FROM trip_route_stop_logs trsl " +
            "    JOIN route_stops rs_inner ON trsl.route_stop_id = rs_inner.id " +
            "    WHERE trsl.trip_id = t.id" +
            ") " +
            "WHERE t.route_id = my_stop.route_id " +
            "AND t.trip_date = :today " +
            "AND t.status IN ('IN_PROGRESS', 'SCHEDULED') " +
            "ORDER BY t.created_at DESC LIMIT 1", nativeQuery = true)
    Map<String, Object> findActiveTripStatus(@Param("studentId") Integer studentId, @Param("today") String today);
}
