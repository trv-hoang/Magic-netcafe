package com.netcafe.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AIService {

    private final Map<Pattern, String> rules = new HashMap<>();

    public AIService() {
        // Pricing
        rules.put(Pattern.compile("(?i).*(price|cost|how much|rate).*"),
                "Our standard rate is 10,000 VND/hour. VIP members get special discounts!");

        // Food & Drink
        rules.put(Pattern.compile("(?i).*(food|drink|hungry|thirsty|menu|eat).*"),
                "We have a great selection of snacks and drinks! Check out the 'Order' tab to see our menu. My favorite is the Mixed Noodles!");

        // Technical Support
        rules.put(Pattern.compile("(?i).*(slow|lag|internet|wifi|network|bug|error).*"),
                "I'm sorry to hear that. Please try restarting your computer. If the issue persists, I'll notify an admin immediately.");

        // Account/Login
        rules.put(Pattern.compile("(?i).*(password|login|account|cant login).*"),
                "If you're having trouble logging in, please ask the staff at the counter to reset your password.");

        // Hours
        rules.put(Pattern.compile("(?i).*(open|close|hours|time).*"),
                "We are open 24/7! Game on whenever you want.");

        // Greetings
        rules.put(Pattern.compile("(?i).*(hello|hi|hey|greetings).*"),
                "Hello! I'm the NetCafe AI Assistant. How can I help you today?");

        // Thanks
        rules.put(Pattern.compile("(?i).*(thank|thx).*"),
                "You're welcome! Enjoy your gaming session.");
    }

    public String getResponse(String userMessage) {
        for (Map.Entry<Pattern, String> entry : rules.entrySet()) {
            if (entry.getKey().matcher(userMessage).matches()) {
                return entry.getValue();
            }
        }
        return null; // No match found
    }
}
