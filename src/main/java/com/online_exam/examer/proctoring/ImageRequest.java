package com.online_exam.examer.proctoring;

import lombok.Data;

@Data
public class ImageRequest {
    private String studentId;
    private String examId;
    private String image;
}
