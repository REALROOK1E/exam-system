package com.zekai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建测验请求DTO
 */
public class CreateQuizRequest {

    @NotNull(message = "教室ID不能为空")
    private Long classroomId;

    @NotBlank(message = "测验标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "开始时间不能为空")
    private String startTime; // yyyy-MM-dd HH:mm:ss

    @NotNull(message = "结束时间不能为空")
    private String endTime;

    @NotNull(message = "答题时长不能为空")
    private Integer durationMinutes;

    @NotNull(message = "总分不能为空")
    private Integer totalPoints;

    @NotNull(message = "及格分不能为空")
    private Integer passingScore;

    private List<QuizQuestion> questions;
    private QuizSettings settings;

    // Getters and Setters
    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public QuizSettings getSettings() {
        return settings;
    }

    public void setSettings(QuizSettings settings) {
        this.settings = settings;
    }

    /**
     * 测验题目内部类
     */
    public static class QuizQuestion {
        private Long questionId;
        private Integer questionOrder;
        private Integer points;

        public QuizQuestion() {}

        public QuizQuestion(Long questionId, Integer questionOrder, Integer points) {
            this.questionId = questionId;
            this.questionOrder = questionOrder;
            this.points = points;
        }

        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Integer getQuestionOrder() {
            return questionOrder;
        }

        public void setQuestionOrder(Integer questionOrder) {
            this.questionOrder = questionOrder;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }
    }

    /**
     * 测验设置内部类
     */
    public static class QuizSettings {
        private Boolean shuffleQuestions = false;
        private Boolean shuffleOptions = false;
        private Boolean showResultsImmediately = false;
        private Boolean allowReview = true;

        public QuizSettings() {}

        // Getters and Setters
        public Boolean getShuffleQuestions() {
            return shuffleQuestions;
        }

        public void setShuffleQuestions(Boolean shuffleQuestions) {
            this.shuffleQuestions = shuffleQuestions;
        }

        public Boolean getShuffleOptions() {
            return shuffleOptions;
        }

        public void setShuffleOptions(Boolean shuffleOptions) {
            this.shuffleOptions = shuffleOptions;
        }

        public Boolean getShowResultsImmediately() {
            return showResultsImmediately;
        }

        public void setShowResultsImmediately(Boolean showResultsImmediately) {
            this.showResultsImmediately = showResultsImmediately;
        }

        public Boolean getAllowReview() {
            return allowReview;
        }

        public void setAllowReview(Boolean allowReview) {
            this.allowReview = allowReview;
        }
    }
}

