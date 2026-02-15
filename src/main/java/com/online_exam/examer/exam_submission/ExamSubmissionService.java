package com.online_exam.examer.exam_submission;

import com.online_exam.examer.authentication.EmailService;
import com.online_exam.examer.exam.ExamEntity;
import com.online_exam.examer.exam.ExamRepository;
import com.online_exam.examer.exam.ExamService;
import com.online_exam.examer.exam_submission.dto.ExamQuestionsDto;
import com.online_exam.examer.exam_submission.dto.LoadExamDto;
import com.online_exam.examer.exam_submission.dto.UserExamDetailsDto;
import com.online_exam.examer.exam_submission.request.AssignExamToUserAddRequest;
import com.online_exam.examer.exam_submission.request.AssignExamToUserUpdateRequest;
import com.online_exam.examer.exam_submission.request.ResetExamToUserRequest;
import com.online_exam.examer.exception.AlreadyExsistsException;
import com.online_exam.examer.exception.ExamExpiredException;
import com.online_exam.examer.exception.ResourceNotFoundException;
import com.online_exam.examer.mapper.EntityToDtoMapper;
import com.online_exam.examer.mapper.PageDto;
import com.online_exam.examer.question.QuestionEntity;
import com.online_exam.examer.question.QuestionRepository;
import com.online_exam.examer.question.enums.QuestionType;
import com.online_exam.examer.user.UserDto;
import com.online_exam.examer.user.UserEntity;
import com.online_exam.examer.user.UserRepository;

import com.online_exam.examer.user_answers.UserAnswerEntity;
import com.online_exam.examer.user_answers.UserAnswerRepository;
import com.online_exam.examer.user_answers.request.RateWrittenExamRequest;
import com.online_exam.examer.user_answers.request.WrittenAnswerRateRequest;
import com.online_exam.examer.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ExamSubmissionService implements IExamSubmissionService {

    private final ExamSubmissionRepository examSubmissionRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final EntityToDtoMapper entityToDtoMapper;
    private final QuestionRepository questionRepositary;
    private final EmailService emailService;
    private final EncryptionUtil encryptionUtil;
    private final Environment environment;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamService examService;


    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = "findUsersByAdminIdAndStatusFalse", allEntries = true),
//            @CacheEvict(value = "findUsersByAdminIdAndNotAssigned", allEntries = true),
//    })
    @Override
    public PageDto<UserDto> assignExamToUser(AssignExamToUserAddRequest assignExamToUserAddRequest, Pageable pageable) {

        // Check if user exists
        if (!userRepository.existsById(assignExamToUserAddRequest.getUserId())) {
            throw new ResourceNotFoundException("User not found ");
        }

        // Retrieve User
        UserEntity user = userRepository.findById(assignExamToUserAddRequest.getUserId()).get();

        // Check if exam exists
        if (!examRepository.existsById(assignExamToUserAddRequest.getExamId())) {
            throw new ResourceNotFoundException(" Exam not found ");
        }

        // Retrieve Exam
        ExamEntity exam = examRepository.findById(assignExamToUserAddRequest.getExamId()).get();
        // Check if the user is already assigned to this exam
        if (examSubmissionRepository.existsByUser_UserIdAndExam_ExamId(user.getUserId(), exam.getExamId())) {
            throw new ResourceNotFoundException("User is already assigned to the same exam  " );
        }
            // Create ExamSubmissionEntity to link the user and exam
            ExamSubmissionEntity examSubmission = new ExamSubmissionEntity();
            examSubmission.setUser(user);
            examSubmission.setExam(exam);
            examSubmission.setScore(0);  // Default score, if applicable
            examSubmission.setStatus(false);  // Default status, e.g., not started or incomplete
            examSubmission.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            //LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(3);
            LocalDateTime expiryDate = LocalDateTime.now().plusWeeks(1);
            examSubmission.setExpiresAt(Timestamp.valueOf(expiryDate));

            ExamSubmissionEntity savedExamSubmission = examSubmissionRepository.save(examSubmission);

            String encryptedId = encryptionUtil.encryptId(savedExamSubmission.getExamSubmissionId());
            encryptionUtil.decryptId(encryptedId);


        //final String examLink = environment.getProperty("public.url")+"exam/takeExam?id="+encryptedId;
        final String examLink = environment.getProperty("public.url")+"exam/takeExam?id="+encryptedId+"&userId="+user.getUserId();

        emailService.sendExamLinkToUser(user.getEmail(),examLink,exam.getExamTitle(),exam.getExamDuration());
        System.out.println("Exam Link (Local Testing): " + examLink);


        // Fetch the all users with pagination
        Page<UserEntity> users = userRepository.findUsersByExamIdAndSubmissionStatus(exam.getExamId(),user.getAdmin().getAdminId(),pageable);

        // Convert the Page<UserEntity> to Page<UserDto>
        Page<UserDto> usersPageDto = entityToDtoMapper.usersPageToDtoPage(users);

        // Wrap the Page<UserDto> into PageDto<UserDto> to return only necessary data
        return new PageDto<>(usersPageDto);
    }




    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = "findUsersByAdminIdAndStatusFalse", allEntries = true),
//            @CacheEvict(value = "getUsersForExamAndAdmin", allEntries = true)
//    })
    @Override
    public void updateExamToUser(String encryptedExamSubmissionId, AssignExamToUserUpdateRequest assignExamToUserUpdateRequest) {

        if(!examSubmissionRepository.existsById(encryptionUtil.decryptId(encryptedExamSubmissionId))){
            throw new ResourceNotFoundException("Exam submission not found");
        }
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(encryptionUtil.decryptId(encryptedExamSubmissionId)).get();

        if(examSubmission.isStatus())
        {
            throw new AlreadyExsistsException("You already took this exam before");
        }

        examSubmission.setScore(assignExamToUserUpdateRequest.getScore());
        examSubmission.setStatus(true);
        examSubmissionRepository.save(examSubmission);


    }

    @Transactional
    @Override
    public void resetExamToUser(ResetExamToUserRequest resetExamToUserRequest) {

        if (!examSubmissionRepository.existsByUser_UserIdAndExam_ExamId(resetExamToUserRequest.getUserId(), resetExamToUserRequest.getExamId()) ) {
            throw new ResourceNotFoundException("User or Exam not found ");
        }

        ExamSubmissionEntity examSubmission = examSubmissionRepository.findByUser_UserIdAndExam_ExamId(resetExamToUserRequest.getUserId(), resetExamToUserRequest.getExamId());
        examSubmission.setScore(0);
        examSubmission.setStatus(false);
        examSubmissionRepository.save(examSubmission);

    }


//    @Transactional(readOnly = true)
////   @Cacheable(value = "getUsersForExamAndAdmin", key = "#examId + '-' + #adminId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
//    @Override
//    public PageDto<UserExamDetailsDto> getUsersForExamAndAdmin(Long examId, Long adminId, Pageable pageable) {
//
//        Page<UserExamDetailsDto> userExamDetails = examSubmissionRepository.findUserExamDetailsByExamAndAdmin(examId, adminId, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("user.userName").ascending()));
//
//        return new PageDto<>(userExamDetails);
//    }


    @Override
    @Transactional(readOnly = true)
    public PageDto<UserExamDetailsDto> getUsersForExamAndAdmin(Long examId, Long adminId, Pageable pageable) {

        Page<UserExamDetailsDto> submissions =
                examSubmissionRepository.findUserExamDetailsByExamAndAdmin(
                        examId,
                        adminId,
                        PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by("user.userName").ascending()
                        )
                );

        int totalMark = examService.calculateTotalMark(examId);

        Page<UserExamDetailsDto> dtoPage = submissions.map(sub -> {
            UserExamDetailsDto dto = new UserExamDetailsDto();

            dto.setExamSubmissionId(sub.getExamSubmissionId());
            dto.setUserId(sub.getUserId());
            dto.setUserName(sub.getUserName());
            dto.setExamName(sub.getExamName());
            dto.setScore(sub.getScore());
            dto.setTotalMark(totalMark);


            return dto;
        });

        return new PageDto<>(dtoPage);
    }




    @Transactional(readOnly = true)
    @Override
    public LoadExamDto getExamQuestionsByExamSubmissionId(String encryptedExamSubmissionId) {

        // Check if the exam submission exists, otherwise throw an exception
        if (!examSubmissionRepository.existsById(encryptionUtil.decryptId(encryptedExamSubmissionId))) {
            throw new ResourceNotFoundException("Exam submission not found");
        }



        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(encryptionUtil.decryptId(encryptedExamSubmissionId)).get();

        if(examSubmission.isStatus())
        {
            throw new AlreadyExsistsException("You already took this exam before");
        }

        if (examSubmission.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ExamExpiredException("Sorry, this exam link has expired.");
        }


        Long examId = examSubmission.getExam().getExamId();

        // Retrieve the exam questions associated with the exam ID
        List<QuestionEntity> questions = questionRepositary.findQuestionsByExamId(examId);

       List<ExamQuestionsDto> examQuestions = entityToDtoMapper.questionsToExamQuestionsDtoList(questions);

        // Create and populate LoadExamDto
        LoadExamDto loadExamDto = new LoadExamDto();
        loadExamDto.setExamDuration(examSubmission.getExam().getExamDuration());  // Set exam duration
        loadExamDto.setExamTitle(examSubmission.getExam().getExamTitle());
        loadExamDto.setUserName(examSubmission.getUser().getUserName());
        loadExamDto.setExamQuestions(examQuestions);  // Set mapped question DTOs

        return loadExamDto;
    }



    public void rateWrittenExam(Long submissionId, RateWrittenExamRequest request) {

        ExamSubmissionEntity submission = examSubmissionRepository
                .findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Remove old written scores from submission.score
        List<UserAnswerEntity> writtenAnswers = userAnswerRepository.findByExamSubmission_ExamSubmissionId(submissionId)
                .stream()
                .filter(ans -> ans.getWrittenAnswer() != null && !ans.getWrittenAnswer().isEmpty())
                .toList();

        int oldWrittenTotal = writtenAnswers.stream()
                .mapToInt(UserAnswerEntity::getWrittenScore)
                .sum();

        // Reset the old written scores
        submission.setScore(submission.getScore() - oldWrittenTotal);

        int newWrittenTotal = 0;

        for (WrittenAnswerRateRequest r : request.getRates()) {

            if (r.getRate() < 1 || r.getRate() > 5) {
                throw new IllegalArgumentException("Rate must be between 1 and 5");
            }

            UserAnswerEntity answer = userAnswerRepository
                    .findById(r.getUserAnswerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

            answer.setWrittenScore(r.getRate());
            newWrittenTotal += r.getRate();

            userAnswerRepository.save(answer);
        }

        submission.setScore(submission.getScore() + newWrittenTotal);
        submission.setStatus(true); // finalized

        examSubmissionRepository.save(submission);
    }





}
