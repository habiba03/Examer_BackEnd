package com.online_exam.examer.exam_submission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExamDetailsDto implements Serializable {

    private Long examSubmissionId;
    private Long userId;
    private String userName;
    private String examName;
    private int totalMark;
    private int score;

    // ✅ JPQL constructor
    public UserExamDetailsDto(
            Long examSubmissionId,
            Long userId,
            String userName,
            String examName,
            int score
    ) {
        this.examSubmissionId = examSubmissionId;
        this.userId = userId;
        this.userName = userName;
        this.examName = examName;
        this.score = score;
    }

    // ✅ ADD THIS (for submit exam response)
    public UserExamDetailsDto(String userName,String examName, int score) {
        this.userName = userName;
        this.examName = examName;
        this.score = score;
    }


}