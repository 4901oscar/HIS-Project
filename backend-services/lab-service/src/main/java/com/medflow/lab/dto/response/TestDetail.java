package com.medflow.lab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDetail {

    private String testName;
    private String testType;
    private String sampleType;
    private boolean hasResult;
}
