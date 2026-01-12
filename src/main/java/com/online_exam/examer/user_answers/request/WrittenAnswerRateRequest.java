package com.online_exam.examer.user_answers.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrittenAnswerRateRequest {
    private Integer userAnswerId;
    private Integer rate; // 1–5
}
