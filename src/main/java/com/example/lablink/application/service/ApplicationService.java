package com.example.lablink.application.service;

import com.example.lablink.application.dto.Request.ApplicationRequestDto;
import com.example.lablink.application.dto.Response.ApplicationResponseDto;
import com.example.lablink.application.entity.Application;
import com.example.lablink.application.exception.ApplicationErrorCode;
import com.example.lablink.application.exception.ApplicationException;
import com.example.lablink.application.repository.ApplicationRepository;
import com.example.lablink.study.entity.Study;
import com.example.lablink.study.service.GetStudyService;
import com.example.lablink.user.entity.User;
import com.example.lablink.user.security.UserDetailsImpl;
import com.example.lablink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final GetStudyService getStudyService;

    //신청서 추가
    @Transactional
    public void addApplication(UserDetailsImpl userDetails, Long studyId, ApplicationRequestDto applicationRequestDto) {
        applicationRepository.save(new Application(userDetails.getUser(),studyId,applicationRequestDto.getMessage()));
        // studyid들고와서 currentApplicantCount +1 해주기
        Study study = getStudyService.getStudy(studyId);
        study.updateCurrentApplicantCount();
    }
    //신청서 수정
    @Transactional
    public void modifyApplication(UserDetailsImpl userDetails, Long studyId,ApplicationRequestDto applicationRequestDto,Long applicationId) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(
                ()->new ApplicationException(ApplicationErrorCode.Application_NOT_FOUND)
        );

        if(!application.getStudyId().equals(studyId) || !application.getUser().getId().equals(userDetails.getUser().getId())){
            throw new ApplicationException(ApplicationErrorCode.NOT_AUTHOR);
        }

        application.update(applicationRequestDto.getMessage());
    }
    //신청서 삭제
    @Transactional
    public void deleteApplication(UserDetailsImpl userDetails, Long studyId,Long applicationId) {
        Application application =applicationRepository.findById(applicationId).orElseThrow(
                ()->new ApplicationException(ApplicationErrorCode.Application_NOT_FOUND)
        );

        if(!application.getStudyId().equals(studyId) || !application.getUser().getId().equals(userDetails.getUser().getId())){
          throw new ApplicationException(ApplicationErrorCode.NOT_AUTHOR);
        }
        applicationRepository.delete(application);
    }
    //신청서 조회
    @Transactional(readOnly = true)
    public Application getApplication(UserDetailsImpl userDetails, Long studyId,Long applicationId) {
        Application application = applicationRepository.findByIdAndUserEmail(applicationId,userDetails.getUser().getEmail()).orElseThrow(
                ()->new ApplicationException(ApplicationErrorCode.Application_NOT_FOUND)
        );
        if(!application.getStudyId().equals(studyId) || !application.getUser().getId().equals(userDetails.getUser().getId())){

            throw new ApplicationException(ApplicationErrorCode.NOT_AUTHOR);
        }

        return application;

    }
    @Transactional(readOnly = true)
    public ApplicationResponseDto afterApplication(UserDetailsImpl userDetails, Long studyId) {
        User user = userService.getUser(userDetails);
        Study study = getStudyService.getStudy(studyId);
        return new ApplicationResponseDto(study.getCompany(),study,user,user.getUserinfo());
    }

    // 내가 쓴 신청서 확인
    public List<Application> findAllByMyApplication(User user) {
        return applicationRepository.findAllByUser(user);
    }

    public void deleteApplication(Application application) {
        applicationRepository.delete(application);
    }


}