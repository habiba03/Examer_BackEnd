package com.online_exam.examer.proctoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse {

    private Resource images;
//    private String imageUrl;

}
