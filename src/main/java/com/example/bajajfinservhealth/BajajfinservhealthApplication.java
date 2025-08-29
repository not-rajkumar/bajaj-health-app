package com.example.bajajfinservhealth;

import com.example.bajajfinservhealth.dto.SubmissionRequest;
import com.example.bajajfinservhealth.dto.WebhookRequest;
import com.example.bajajfinservhealth.dto.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BajajfinservhealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajfinservhealthApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Generate Webhook with your details
            String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            // Using your registration number: REG0978
            WebhookRequest webhookRequest = new WebhookRequest("M Rajkumar", "22BCE0978", "rajkumar52k4@gmail.com");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<WebhookRequest> requestEntity = new HttpEntity<>(webhookRequest, headers);

            System.out.println("Sending initial request to generate webhook...");
            WebhookResponse webhookResponse = restTemplate.postForObject(generateWebhookUrl, requestEntity, WebhookResponse.class);

            if (webhookResponse != null) {
                System.out.println("Webhook URL Received: " + webhookResponse.getWebhookUrl());
                System.out.println("Access Token Received.");

                // 2. Determine SQL Query based on Registration Number
                String regNo = webhookRequest.getRegNo();
                // Extracting the last two digits from "REG0978" -> "78"
                int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
                
                String finalQuery;
                if (lastTwoDigits % 2 != 0) {
                    // Odd Number - Question 1
                    finalQuery = "WITH RankedSalaries AS ( " +
                                 "  SELECT " +
                                 "    e.Name AS Employee, " +
                                 "    e.Salary, " +
                                 "    d.Name AS Department, " +
                                 "    DENSE_RANK() OVER (PARTITION BY d.Id ORDER BY e.Salary DESC) AS RankNum " +
                                 "  FROM Employee e " +
                                 "  JOIN Department d ON e.DepartmentId = d.Id " +
                                 ") " +
                                 "SELECT " +
                                 "  Department, " +
                                 "  Employee, " +
                                 "  Salary " +
                                 "FROM RankedSalaries " +
                                 "WHERE RankNum <= 3;";
                } else {
                    // Even Number - Question 2
                    System.out.println("Registration number ends in an even number. Using Query for Question 2.");
                    finalQuery = "SELECT DISTINCT p1.Email " +
                                 "FROM Person p1, Person p2 " +
                                 "WHERE p1.Email = p2.Email AND p1.Id <> p2.Id;";
                }
                System.out.println("Final SQL Query to be submitted: " + finalQuery);


                // 3. Submit the solution
                String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
                SubmissionRequest submissionRequest = new SubmissionRequest(finalQuery);

                HttpHeaders submissionHeaders = new HttpHeaders();
                submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
                submissionHeaders.set("Authorization", webhookResponse.getAccessToken());

                HttpEntity<SubmissionRequest> submissionEntity = new HttpEntity<>(submissionRequest, submissionHeaders);
                
                System.out.println("Submitting final query to the webhook...");
                String submissionResponse = restTemplate.postForObject(submitUrl, submissionEntity, String.class);
                System.out.println("Submission Response: " + submissionResponse);
            } else {
                 System.err.println("Failed to get a response from the webhook generation URL.");
            }
        };
    }
}
