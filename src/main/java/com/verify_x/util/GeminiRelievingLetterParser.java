package com.verify_x.util;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class GeminiRelievingLetterParser {

    private final ChatClient chatClient;

    public GeminiRelievingLetterParser(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String parseRelievingLetter(String relievingLetterText) {

        String prompt = """
You are an AI that extracts information from Relieving Letters.

Return ONLY a valid JSON object.

Rules:

1. Do NOT return markdown.
2. Do NOT return ```json.
3. Do NOT explain anything.
4. Return ONLY JSON.
5. If any field is not available, return an empty string "".
6. Return all dates in yyyy-MM-dd format.
7. Do NOT return arrays.

Extract the following fields:

{
  "employeeName":"",
  "companyName":"",
  "designation":"",
  "employeeId":"",
  "joiningDate":"",
  "relievingDate":"",
  "reasonForLeaving":"",
  "hrName":"",
  "hrEmail":"",
  "workLocation":""
}

Relieving Letter:

""" + relievingLetterText;

        try {

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null) {
                throw new RuntimeException(
                        "Gemini returned null response.");
            }

            response = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            System.out.println("========== GEMINI RELIEVING LETTER RESPONSE ==========");
            System.out.println(response);

            return response;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to parse Relieving Letter using Gemini : "
                            + e.getMessage(), e);
        }
    }
}