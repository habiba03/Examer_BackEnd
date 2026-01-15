package com.online_exam.examer.proctoring;

import com.online_exam.examer.exam.ExamEntity;
import com.online_exam.examer.exam.ExamRepository;
import com.online_exam.examer.exam_submission.ExamSubmissionEntity;
import com.online_exam.examer.exam_submission.ExamSubmissionRepository;
import com.online_exam.examer.user.UserEntity;
import com.online_exam.examer.user.UserRepository;
import com.online_exam.examer.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProctoringService {

    private final ProctoringRepository proctoringRepository;
    private final FileStorageService fileStorageService;
    private final EncryptionUtil encryptionUtil;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;

    public void saveScreenshot(Long studentId, String examEncryptedId, String image) throws IOException {
        Long examSubmissionId = encryptionUtil.decryptId(examEncryptedId);
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
}