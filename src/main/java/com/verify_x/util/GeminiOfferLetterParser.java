//package com.verify_x.util;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GeminiOfferLetterParser {
//
//    private static final Logger log = LoggerFactory.getLogger(GeminiOfferLetterParser.class);
//
//    private final ChatClient chatClient;
//
//    public GeminiOfferLetterParser(ChatClient chatClient) {
//        this.chatClient = chatClient;
//    }
//
//    public String parseOfferLetter(String offerLetterText) {
//
//        String prompt = """
//You are an AI that extracts information from an Offer Letter.
//
//Return ONLY a valid JSON object.
//
//Rules:
//- Do NOT return markdown.
//- Do NOT return ```json.
//- Do NOT explain anything.
//- Return ONLY JSON.
//- If a field is missing, return an empty string "".
//- Return annualCTC as a number.
//- Return offerDate and joiningDate ONLY in yyyy-MM-dd format.
//
//JSON Format:
//
//{
//  "candidateName":"",
//  "companyName":"",
//  "designation":"",
//  "annualCTC":0,
//  "offerDate":"",
//  "joiningDate":"",
//  "offerLetterNumber":"",
//  "hrName":"",
//  "hrEmail":"",
//  "workLocation":""
//}
//
//Offer Letter:
//
//""" + offerLetterText;
//
//        try {
//
//            String response = chatClient.prompt()
//                    .user(prompt)
//                    .call()
//                    .content();
//
//            if (response == null) {
//                throw new RuntimeException("Gemini returned null response.");
//            }
//
//            response = response
//                    .replace("```json", "")
//                    .replace("```", "")
//                    .trim();
//
//            log.debug("Gemini offer letter response: {}", response);
//
//            return response;
//
//        } catch (Exception e) {
//
//            log.error("Failed to parse offer letter using Gemini", e);
//
//            throw new RuntimeException(
//                    "Failed to parse Offer Letter using Gemini.",
//                    e);
//        }
//    }
//}