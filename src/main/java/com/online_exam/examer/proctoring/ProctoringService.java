package com.online_exam.examer.proctoring;

import com.online_exam.examer.exam.ExamEntity;
import com.online_exam.examer.exam.ExamRepository;
import com.online_exam.examer.exam_submission.ExamSubmissionEntity;
import com.online_exam.examer.exam_submission.ExamSubmissionRepository;
import com.online_exam.examer.user.UserEntity;
import com.online_exam.examer.user.UserRepository;
import com.online_exam.examer.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProctoringService {

//    private static final ProctoringRepository proctoringRepository = null;
private final ProctoringRepository proctoringRepository;

    private final FileStorageService fileStorageService;
    private final EncryptionUtil encryptionUtil;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

    public void saveScreenshot(String studentEncryptedId, String examEncryptedId, String image) throws IOException {
        Long examSubmissionId = encryptionUtil.decryptId(examEncryptedId);
        Long studentId = encryptionUtil.decryptId(studentEncryptedId);
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(examSubmissionId).orElseThrow();

        ExamEntity exam = examRepository.findById(examSubmission.getExam().getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + examSubmissionId));
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + studentId));


        String path = fileStorageService.saveBase64Image(exam.getExamId(), studentId, image);


        ProctoringEntity log = new ProctoringEntity();
        log.setUser(student);
        log.setExam(exam);
        log.setImageUrl(path);
        log.setTimestamp(LocalDateTime.now());
        proctoringRepository.save(log);
    }


//    public List<ImageResponse> getStudentImagesInExam(Long examId, Long studentId) {
//        List<ProctoringEntity> proctoringEntities = proctoringRepository.findByExam_ExamIdAndUser_UserId(examId, studentId);
//        List<Resource> resources = proctoringEntities.stream()
//                .map(x -> fileStorageService.loadImageAsResource(x.getImageUrl()))
//                .toList();
//        List<ImageResponse> imageResponses = resources.stream().map(
//                ImageResponse::new
//        ).toList();
//
//        return imageResponses;
//    }

    public List<String> getStudentImagesInExam(Long examId, Long studentId) throws IOException {

        List<ProctoringEntity> proctoringEntities =
                proctoringRepository.findByExam_ExamIdAndUser_UserId(examId, studentId);

        List<String> imageResponses = new ArrayList<>();

        for (ProctoringEntity entity : proctoringEntities) {
            Resource resource = fileStorageService.loadImageAsResource(entity.getImageUrl());

            byte[] bytes = resource.getContentAsByteArray();

            String base64Image = Base64.getEncoder().encodeToString(bytes);

            imageResponses.add(base64Image);
        }

        return imageResponses;
    }


//public List<ImageResponse> getStudentImagesInExam(Long examId, Long studentId) {
//
//    return proctoringRepository
//            .findByExam_ExamIdAndUser_UserId(examId, studentId)
//            .stream()
//            .map(p -> new ImageResponse(
//                    p.getImageUrl()))
//            .toList();
//}


}
