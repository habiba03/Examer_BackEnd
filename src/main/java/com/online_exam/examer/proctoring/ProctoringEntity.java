package com.online_exam.examer.proctoring;

import com.online_exam.examer.exam.ExamEntity;
import com.online_exam.examer.user.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ProctoringEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private ExamEntity exam;
    private String imageUrl; // سنخزن مسار الملف هنا وليس الصورة نفسها
    private LocalDateTime timestamp;
}