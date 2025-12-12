package com.zekai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建题目请求DTO
 */
public class CreateQuestionRequest {

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    @NotBlank(message = "题目内容不能为空")
    private String questionText;

    @NotBlank(message = "题目类型不能为空")
    private String questionType; // multiple_choice, true_false, essay, fill_blank, short_answer

    @NotNull(message = "难度等级不能为空")
    private Integer difficultyLevel; // 1-5

    private List<QuestionOption> options;

    // Getters and Setters
    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    /**
     * 题目选项内部类
     */
    public static class QuestionOption {
        private String optionText;
        private Boolean isCorrect;
        private Integer optionOrder;

        public QuestionOption() {}

        public QuestionOption(String optionText, Boolean isCorrect, Integer optionOrder) {
            this.optionText = optionText;
            this.isCorrect = isCorrect;
            this.optionOrder = optionOrder;
        }

        // Getters and Setters
        public String getOptionText() {
            return optionText;
        }

        public void setOptionText(String optionText) {
            this.optionText = optionText;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }

        public Integer getOptionOrder() {
            return optionOrder;
        }

        public void setOptionOrder(Integer optionOrder) {
            this.optionOrder = optionOrder;
        }
    }
}

