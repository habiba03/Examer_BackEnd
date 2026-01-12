package com.online_exam.examer.user_answers.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateWrittenExamRequest {
    private List<WrittenAnswerRateRequest> rates;
}
