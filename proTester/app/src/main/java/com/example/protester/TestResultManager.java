package com.example.protester;

import android.content.Context;
import android.os.Environment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestResultManager {
    private Context context;
    private List<TestResult> testResults;

    public TestResultManager(Context context) {
        this.context = context;
        this.testResults = new ArrayList<>();
    }

    public void addTestResult(TestResult result) {
        testResults.add(result);
    }

    public List<TestResult> getTestResults() {
        return new ArrayList<>(testResults);
    }

    public void clearResults() {
        testResults.clear();
    }

    public boolean saveResultsAsJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray resultsArray = new JSONArray();

            for (TestResult result : testResults) {
                JSONObject resultObject = new JSONObject();
                resultObject.put("test_id", result.getTestId());
                resultObject.put("test_name", result.getTestName());
                resultObject.put("passed", result.isPassed());
                resultObject.put("start_time", result.getStartTime().getTime());
                resultObject.put("end_time", result.getEndTime().getTime());
                resultObject.put("duration", result.getDuration());
                resultObject.put("error_message", result.getErrorMessage());
                resultObject.put("details", result.getDetails());

                resultsArray.put(resultObject);
            }

            jsonObject.put("test_results", resultsArray);
            jsonObject.put("total_tests", testResults.size());
            jsonObject.put("passed_tests", getPassedCount());
            jsonObject.put("failed_tests", getFailedCount());
            jsonObject.put("test_date", System.currentTimeMillis());

            return saveJsonToFile(jsonObject.toString(), "test_results.json");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveResultsAsXml() {
        try {
            StringBuilder xmlBuilder = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlBuilder.append("<test_results>\n");
            xmlBuilder.append("  <summary>\n");
            xmlBuilder.append("    <total_tests>").append(testResults.size()).append("</total_tests>\n");
            xmlBuilder.append("    <passed_tests>").append(getPassedCount()).append("</passed_tests>\n");
            xmlBuilder.append("    <failed_tests>").append(getFailedCount()).append("</failed_tests>\n");
            xmlBuilder.append("    <test_date>").append(dateFormat.format(new Date())).append("</test_date>\n");
            xmlBuilder.append("  </summary>\n");

            for (TestResult result : testResults) {
                xmlBuilder.append("  <test>\n");
                xmlBuilder.append("    <id>").append(result.getTestId()).append("</id>\n");
                xmlBuilder.append("    <name>").append(result.getTestName()).append("</name>\n");
                xmlBuilder.append("    <passed>").append(result.isPassed()).append("</passed>\n");
                xmlBuilder.append("    <start_time>").append(dateFormat.format(result.getStartTime())).append("</start_time>\n");
                xmlBuilder.append("    <end_time>").append(dateFormat.format(result.getEndTime())).append("</end_time>\n");
                xmlBuilder.append("    <duration>").append(result.getDuration()).append("ms</duration>\n");

                if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
                    xmlBuilder.append("    <error_message>").append(escapeXml(result.getErrorMessage())).append("</error_message>\n");
                }

                if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                    xmlBuilder.append("    <details>").append(escapeXml(result.getDetails())).append("</details>\n");
                }

                xmlBuilder.append("  </test>\n");
            }

            xmlBuilder.append("</test_results>");

            return saveJsonToFile(xmlBuilder.toString(), "test_results.xml");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    private boolean saveJsonToFile(String content, String filename) {
        try {
            File storageDir = new File(Environment.getExternalStorageDirectory(), "ProTester");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File resultFile = new File(storageDir, filename);
            FileWriter writer = new FileWriter(resultFile);
            writer.write(content);
            writer.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getPassedCount() {
        int count = 0;
        for (TestResult result : testResults) {
            if (result.isPassed()) {
                count++;
            }
        }
        return count;
    }

    private int getFailedCount() {
        int count = 0;
        for (TestResult result : testResults) {
            if (!result.isPassed()) {
                count++;
            }
        }
        return count;
    }

    public String getSummary() {
        int total = testResults.size();
        int passed = getPassedCount();
        int failed = getFailedCount();
        return String.format("总计: %d, 通过: %d, 失败: %d", total, passed, failed);
    }
}