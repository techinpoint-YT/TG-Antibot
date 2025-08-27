package org.spigot.core.advanced;

import org.spigot.TGAntiBotPlugin;
import org.spigot.core.data.PlayerProfile;
import org.spigot.enums.AttackType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Machine Learning Engine for bot detection
 * Uses pattern recognition and behavioral analysis
 */
public class MachineLearningEngine {

    private final TGAntiBotPlugin plugin;
    private final Map<String, BehaviorModel> behaviorModels;
    private final Map<String, Double> featureWeights;
    private final Queue<TrainingData> trainingQueue;
    
    private boolean learningMode;
    private double accuracyThreshold;

    public MachineLearningEngine(TGAntiBotPlugin plugin) {
        this.plugin = plugin;
        this.behaviorModels = new ConcurrentHashMap<>();
        this.featureWeights = new HashMap<>();
        this.trainingQueue = new LinkedList<>();
        this.learningMode = plugin.getConfig().getBoolean("advanced.learning-mode", false);
        this.accuracyThreshold = plugin.getConfig().getDouble("advanced.accuracy-threshold", 0.85);
        
        initializeFeatureWeights();
        startLearningTask();
    }

    private void initializeFeatureWeights() {
        featureWeights.put("connection_frequency", 0.25);
        featureWeights.put("nickname_pattern", 0.20);
        featureWeights.put("timing_regularity", 0.30);
        featureWeights.put("geographic_consistency", 0.15);
        featureWeights.put("behavior_variance", 0.10);
    }

    public double calculateBotProbability(PlayerProfile profile) {
        double score = 0.0;
        
        // Connection frequency analysis
        score += analyzeConnectionFrequency(profile) * featureWeights.get("connection_frequency");
        
        // Nickname pattern analysis
        score += analyzeNicknamePatterns(profile) * featureWeights.get("nickname_pattern");
        
        // Timing regularity analysis
        score += analyzeTimingRegularity(profile) * featureWeights.get("timing_regularity");
        
        // Geographic consistency analysis
        score += analyzeGeographicConsistency(profile) * featureWeights.get("geographic_consistency");
        
        // Behavior variance analysis
        score += analyzeBehaviorVariance(profile) * featureWeights.get("behavior_variance");
        
        return Math.min(1.0, Math.max(0.0, score));
    }

    private double analyzeConnectionFrequency(PlayerProfile profile) {
        List<Long> connectionTimes = profile.getConnectionTimes();
        if (connectionTimes.size() < 3) return 0.0;
        
        // Calculate average interval
        long totalInterval = 0;
        for (int i = 1; i < connectionTimes.size(); i++) {
            totalInterval += connectionTimes.get(i) - connectionTimes.get(i - 1);
        }
        double avgInterval = (double) totalInterval / (connectionTimes.size() - 1);
        
        // Bots tend to have very regular intervals
        if (avgInterval < 5000) { // Less than 5 seconds
            return 0.8;
        } else if (avgInterval < 30000) { // Less than 30 seconds
            return 0.4;
        }
        
        return 0.1;
    }

    private double analyzeNicknamePatterns(PlayerProfile profile) {
        Set<String> nicknames = profile.getNicknames();
        if (nicknames.size() < 2) return 0.0;
        
        double suspicionScore = 0.0;
        
        // Check for sequential patterns (Player1, Player2, etc.)
        List<String> nicknameList = new ArrayList<>(nicknames);
        for (String nickname : nicknameList) {
            if (nickname.matches(".*\\d+$")) { // Ends with numbers
                suspicionScore += 0.3;
            }
            if (nickname.length() < 4) { // Very short names
                suspicionScore += 0.2;
            }
            if (nickname.matches("^[a-zA-Z]+\\d{2,}$")) { // Letters followed by 2+ digits
                suspicionScore += 0.4;
            }
        }
        
        return Math.min(1.0, suspicionScore / nicknames.size());
    }

    private double analyzeTimingRegularity(PlayerProfile profile) {
        List<Long> connectionTimes = profile.getConnectionTimes();
        if (connectionTimes.size() < 4) return 0.0;
        
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < connectionTimes.size(); i++) {
            intervals.add(connectionTimes.get(i) - connectionTimes.get(i - 1));
        }
        
        // Calculate standard deviation
        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = intervals.stream()
            .mapToDouble(interval -> Math.pow(interval - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // Very low standard deviation indicates bot behavior
        if (stdDev < 1000) { // Less than 1 second variation
            return 0.9;
        } else if (stdDev < 5000) { // Less than 5 seconds variation
            return 0.6;
        } else if (stdDev < 15000) { // Less than 15 seconds variation
            return 0.3;
        }
        
        return 0.1;
    }

    private double analyzeGeographicConsistency(PlayerProfile profile) {
        // This would integrate with GeoIP data
        // For now, return a placeholder value
        return 0.0;
    }

    private double analyzeBehaviorVariance(PlayerProfile profile) {
        // Analyze variance in play patterns, command usage, etc.
        long playTime = profile.getPlayTime();
        int connections = profile.getConnectionCount();
        
        if (connections > 10 && playTime < 60000) { // Many connections, little playtime
            return 0.7;
        }
        
        return 0.2;
    }

    public void trainModel(PlayerProfile profile, boolean isBot) {
        if (!learningMode) return;
        
        TrainingData data = new TrainingData(profile, isBot);
        trainingQueue.offer(data);
        
        // Keep training queue size manageable
        while (trainingQueue.size() > 1000) {
            trainingQueue.poll();
        }
    }

    private void startLearningTask() {
        if (!learningMode) return;
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            processTrainingData();
        }, 6000L, 6000L); // Every 5 minutes
    }

    private void processTrainingData() {
        if (trainingQueue.isEmpty()) return;
        
        plugin.getLogger().info("§e[ML Engine] Processing " + trainingQueue.size() + " training samples...");
        
        // Simple weight adjustment based on training data
        int correctPredictions = 0;
        int totalPredictions = 0;
        
        for (TrainingData data : trainingQueue) {
            double prediction = calculateBotProbability(data.profile);
            boolean predictedBot = prediction > 0.5;
            
            if (predictedBot == data.isBot) {
                correctPredictions++;
            }
            totalPredictions++;
        }
        
        double accuracy = (double) correctPredictions / totalPredictions;
        plugin.getLogger().info("§e[ML Engine] Current accuracy: " + String.format("%.2f%%", accuracy * 100));
        
        if (accuracy < accuracyThreshold) {
            adjustWeights();
        }
        
        trainingQueue.clear();
    }

    private void adjustWeights() {
        // Simple weight adjustment algorithm
        plugin.getLogger().info("§e[ML Engine] Adjusting feature weights for better accuracy...");
        
        // This is a simplified approach - in a real ML system, you'd use more sophisticated algorithms
        for (Map.Entry<String, Double> entry : featureWeights.entrySet()) {
            double currentWeight = entry.getValue();
            double adjustment = (Math.random() - 0.5) * 0.1; // Random adjustment between -0.05 and 0.05
            double newWeight = Math.max(0.05, Math.min(0.5, currentWeight + adjustment));
            entry.setValue(newWeight);
        }
        
        // Normalize weights to sum to 1.0
        double totalWeight = featureWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        featureWeights.replaceAll((k, v) -> v / totalWeight);
    }

    public boolean isLearningMode() {
        return learningMode;
    }

    public void setLearningMode(boolean learningMode) {
        this.learningMode = learningMode;
    }

    public Map<String, Double> getFeatureWeights() {
        return new HashMap<>(featureWeights);
    }

    private static class TrainingData {
        final PlayerProfile profile;
        final boolean isBot;

        TrainingData(PlayerProfile profile, boolean isBot) {
            this.profile = profile;
            this.isBot = isBot;
        }
    }

    private static class BehaviorModel {
        final String ip;
        final Map<String, Double> features;
        double botProbability;

        BehaviorModel(String ip) {
            this.ip = ip;
            this.features = new HashMap<>();
            this.botProbability = 0.0;
        }
    }
}