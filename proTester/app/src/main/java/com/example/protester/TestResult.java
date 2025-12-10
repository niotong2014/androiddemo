package com.example.protester;

import java.util.Date;

public class TestResult {
    private String testId;
    private String testName;
    private boolean passed;
    private Date startTime;
    private Date endTime;
    private String errorMessage;
    private String details;

    public TestResult(String testId, String testName) {
        this.testId = testId;
        this.testName = testName;
        this.startTime = new Date();
        this.passed = false;
    }

    public String getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.getTime() - startTime.getTime();
    }
}