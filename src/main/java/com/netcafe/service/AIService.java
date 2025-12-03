package com.netcafe.service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class AIService {

        private final Map<Pattern, String[]> rules = new HashMap<>();
        private final Random random = new Random();

        public AIService() {
                // Pricing
                rules.put(Pattern.compile("(?i).*(price|cost|how much|rate).*"),
                                new String[] {
                                                "Our standard rate is 10,000 VND/hour. VIP members get special discounts!",
                                                "It's just 10,000 VND per hour. Cheap, right?",
                                                "10k VND/hr. Best price in town!"
                                });

                // Technical Support (General)
                rules.put(Pattern.compile("(?i).*(internet|wifi|network|bug|error).*"),
                                new String[] {
                                                "I'm sorry to hear that. Please try restarting your computer.",
                                                "Have you tried turning it off and on again?",
                                                "I'll note that down. If it persists, please call a staff member."
                                });

                // Account/Login
                rules.put(Pattern.compile("(?i).*(password|login|account|cant login).*"),
                                new String[] {
                                                "If you're having trouble logging in, please ask the staff at the counter to reset your password.",
                                                "Login issues? The staff at the counter can help you reset it in seconds."
                                });

                // Hours
                rules.put(Pattern.compile("(?i).*(open|close|hours|time).*"),
                                new String[] {
                                                "We are open 24/7! Game on whenever you want.",
                                                "No closing time here. We're open 24 hours a day!"
                                });

                // Greetings
                rules.put(Pattern.compile("(?i).*(hello|hi|hey|greetings).*"),
                                new String[] {
                                                "Hello! I'm the NetCafe AI Assistant. How can I help you today?",
                                                "Hi there! Ready to game?",
                                                "Greetings! Let me know if you need anything."
                                });

                // Thanks
                rules.put(Pattern.compile("(?i).*(thank|thx).*"),
                                new String[] {
                                                "You're welcome! Enjoy your gaming session.",
                                                "No problem! GLHF!",
                                                "Anytime!"
                                });
        }

        public String getResponse(String userMessage) {
                // 1. Sentiment Analysis
                if (isNegativeSentiment(userMessage)) {
                        return "I apologize for the frustration. I have flagged this conversation for the manager. They will be with you shortly.";
                }

                // 2. Context-Aware Recommendations (Food/Hungry)
                if (userMessage.matches("(?i).*(food|drink|hungry|thirsty|menu|eat|recommend).*")) {
                        return getTimeBasedRecommendation();
                }

                // 3. Rule-based Matching
                for (Map.Entry<Pattern, String[]> entry : rules.entrySet()) {
                        if (entry.getKey().matcher(userMessage).matches()) {
                                String[] responses = entry.getValue();
                                return responses[random.nextInt(responses.length)];
                        }
                }

                return null; // No match found
        }

        private boolean isNegativeSentiment(String text) {
                return text.matches("(?i).*(bad|slow|hate|stupid|broken|terrible|sucks|useless).*");
        }

        private String getTimeBasedRecommendation() {
                LocalTime now = LocalTime.now();
                int hour = now.getHour();

                if (hour >= 5 && hour < 11) {
                        // Morning
                        return "Good morning! It's breakfast time. How about a hot Coffee and a Banh Mi?";
                } else if (hour >= 11 && hour < 14) {
                        // Lunch
                        return "It's lunch time! Our Mixed Noodles are a customer favorite right now.";
                } else if (hour >= 14 && hour < 18) {
                        // Afternoon
                        return "Need an energy boost? Grab a Sting and some Snacks to keep the killstreak going!";
                } else if (hour >= 18 && hour < 22) {
                        // Dinner
                        return "Dinner time! Fuel up with some Fried Rice or Noodles.";
                } else {
                        // Late Night
                        return "Burning the midnight oil? We have energy drinks and instant noodles to keep you awake!";
                }
        }
}
