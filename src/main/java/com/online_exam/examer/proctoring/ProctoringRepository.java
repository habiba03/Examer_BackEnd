package com.online_exam.examer.proctoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProctoringRepository extends JpaRepository<ProctoringEntity, Long> {

}