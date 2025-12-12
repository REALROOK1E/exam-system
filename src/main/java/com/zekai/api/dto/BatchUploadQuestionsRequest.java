package com.zekai.api.dto;

import java.util.List;

/**
 * 批量上传题目请求DTO
 */
public class BatchUploadQuestionsRequest {
    private List<CreateQuestionRequest> questions;

    public BatchUploadQuestionsRequest() {}

    public BatchUploadQuestionsRequest(List<CreateQuestionRequest> questions) {
        this.questions = questions;
    }

    // Getters and Setters
    public List<CreateQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuestionRequest> questions) {
        this.questions = questions;
    }
}

