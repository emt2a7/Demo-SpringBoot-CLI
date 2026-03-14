package org.example.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 目標結構：求職者履歷檔案
 */
public record StructuredRecord (

        @JsonPropertyDescription("求職者的真實全名，如果找不到請填寫 '未知'")
        String fullName,

        @JsonPropertyDescription("總工作年資，請換算成整數的『年』。例如半年為 0，三年半為 3")
        int yearsOfExperience,

        @JsonPropertyDescription("求職者具備的專業技能清單，例如：['Java', 'Spring Boot', 'AWS']")
        List<String> technicalSkills,

        @JsonPropertyDescription("求職者的最高學歷學校名稱")
        String highestSchool,

        @JsonPropertyDescription("請身為資深人資，給予這位求職者一段不超過 50 個字的簡短評價")
        String hrRemark
) {}