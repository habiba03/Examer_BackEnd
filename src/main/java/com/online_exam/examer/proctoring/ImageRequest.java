package com.online_exam.examer.proctoring;

import lombok.Data;

@Data
public class ImageRequest {
    private Long studentId;
    private String examId;
    private String image;
}
