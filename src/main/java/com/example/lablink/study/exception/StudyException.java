package com.example.lablink.study.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyException extends RuntimeException {
    private final StudyErrorCode errorCode;
}