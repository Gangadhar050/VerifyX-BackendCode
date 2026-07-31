//package com.verify_x.util;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GeminiResumeParser {
//
//    private static final Logger log = LoggerFactory.getLogger(GeminiResumeParser.class);
//
//    private final ChatClient chatClient;
//
//    public GeminiResumeParser(ChatClient chatClient) {
//        this.chatClient = chatClient;
//    }
//
//    public String parseResume(String resumeText) {
//
//        String prompt = """
//                 Return ONLY a valid JSON object.
//
//                 Rules:
//                 1. Do NOT return markdown.
//                 2. Do NOT return ```json.
//                 3. Do NOT explain anything.
//                 4. Return ONLY JSON.
//                 5. If any field is missing, use "" for String values.
//                 6. If any numeric value is missing, use 0.
//                 7. skills must be returned as an array of objects.
//                 8. employmentHistory must be returned as an array of objects.
//                 9. Parse all companies mentioned in the resume.
//                 10. Convert ALL dates to yyyy-MM-dd format.
//                11. If only month and year are available (e.g., "Mar 2024"), return the first day of the month (e.g., "2024-03-01").
//                12. If only the year is available (e.g., "2024"), return "2024-01-01".
//                13. If the candidate is currently employed and the resume contains any of these values:
//                    - Present
//                    - present
//                    - PRESENT
//                    - Current
//                    - current
//                    - CURRENT
//                    - Till Date
//                    - till date
//                    - Till Now
//                    - till now
//                    - Ongoing
//                    - ongoing
//                    - Currently Working
//                    - currently working
//                    - Working
//                    - working
//
//                    then ALWAYS return:
//
//                    "relievingDate": "Present"
//
//                14. Never return "Current", "Till Date", "Ongoing", or any other variation. Always use exactly "Present" for an active job.
//                15. If the relieving date is unknown and there is evidence that the candidate is still employed, return "Present".
//
//                 JSON Format:
//
//                 {
//                   "fullName":"",
//                   "email":"",
//                   "phoneNumber":"",
//                   "designation":"",
//                   "totalExperience":0,
//                   "education":"",
//                   "location":"",
//                   "linkedInUrl":"",
//                   "githubUrl":"",
//
//                   "skills":[
//                     {
//                       "skillName":""
//                     }
//                   ],
//
//                   "employmentHistory":[
//                     {
//                       "companyName":"",
//                       "designation":"",
//                       "workLocation":"",
//                       "joiningDate":"",
//                       "relievingDate":"",
//                       "duration":"",
//                       "employmentType":"",
//                       "description":""
//                     }
//                   ],
//
//                   "certifications":[
//                     {
//                       "certificateName":"",
//                       "organization":"",
//                       "issueDate":""
//                     }
//                   ]
//                 }
//
//                 Resume:
//
//                """ + resumeText;
//
//        try {
//
//            String response = chatClient.prompt()
//                    .user(prompt)
//                    .call()
//                    .content();
//
//            log.debug("Gemini resume response: {}", response);
//
//            return response;
//
//        } catch (Exception e) {
//
//            log.error("Failed to parse resume using Gemini", e);
//            throw e;
//        }
//    }
//}