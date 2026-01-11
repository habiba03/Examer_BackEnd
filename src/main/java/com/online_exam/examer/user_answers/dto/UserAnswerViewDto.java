package com.online_exam.examer.user_answers.dto;

import com.online_exam.examer.question.dto.OptionDto;
import com.online_exam.examer.question.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UserAnswerViewDto {

    private Long questionId;
    private String questionText;
    private QuestionType questionType;

    private String writtenAnswer;

    private List<OptionDto> selectedOptions;
}

