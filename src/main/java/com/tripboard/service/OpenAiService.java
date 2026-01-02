package com.tripboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OpenAiService.class);

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * 여행 계획에 대한 AI 추천 생성
     */
    public List<Map<String, Object>> generateTravelRecommendations(String region, String departureDate,
            String arrivalDate, Integer adultCount, Integer childCount, Boolean hasPet) {

        String prompt = buildTravelPrompt(region, departureDate, arrivalDate, adultCount, childCount, hasPet);

        try {
            String response = callOpenAiApi(prompt);
            return parseRecommendations(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 추가 추천 요청
     */
    public List<Map<String, Object>> getAdditionalRecommendations(String region, String category,
            List<String> excludedIds) {

        String prompt = buildAdditionalPrompt(region, category, excludedIds);

        try {
            String response = callOpenAiApi(prompt);
            return parseRecommendations(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private String buildTravelPrompt(String region, String departureDate, String arrivalDate, Integer adultCount,
            Integer childCount, Boolean hasPet) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 대한민국 '").append(region).append("' 지역 전문 여행 가이드입니다.\n");
        prompt.append("다음 조건에 맞춰서 여행 코스를 추천해주세요.\n\n");

        prompt.append("여행지: ").append(region).append("\n");
        prompt.append("여행 시작일: ").append(departureDate).append("\n");
        prompt.append("여행 종료일: ").append(arrivalDate).append("\n");
        prompt.append("성인: ").append(adultCount).append("명\n");
        prompt.append("아동: ").append(childCount).append("명\n");

        if (hasPet != null && hasPet) {
            prompt.append("반려동물 동반: 필수\n");
        }

        prompt.append("\n다음 형식으로 JSON 응답을 해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"destination|accommodation\",\n");
        prompt.append("      \"title\": \"장소 이름\",\n");
        prompt.append("      \"description\": \"한 줄 소개\",\n");
        prompt.append("      \"reason\": \"추천 이유\",\n");
        prompt.append("      \"address\": \"주소 (도로명 주소)\",\n");
        prompt.append("      \"mapX\": \"경도 (필수, 124~132 사이의 실수/문자열, 예: '126.9780')\",\n");
        prompt.append("      \"mapY\": \"위도 (필수, 33~43 사이의 실수/문자열, 예: '37.5665')\",\n");
        prompt.append("      \"image_keyword\": \"이미지 검색용 영문 키워드\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"transportation\": [...] (교통편 정보도 포함)\n");
        prompt.append("}\n");
        prompt.append("\n");
        prompt.append(
                "IMPORTANT: You must provide valid coordinates for mapX and mapY. Do not use null or empty strings.\n");
        prompt.append("Example of valid item:\n");
        prompt.append("{ \"title\": \"N Seoul Tower\", \"mapX\": \"126.9882\", \"mapY\": \"37.5512\", ... }\n");
        prompt.append("\n");
        prompt.append("오직 JSON 데이터만 응답해줘. 마크다운이나 추가 설명 금지.\n");
        return prompt.toString();
    }

    private String buildAdditionalPrompt(String region, String category, List<String> excludedIds) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("한국 ").append(region).append(" 지역의 ").append(category).append("를 추가로 추천해주세요.\n");
        if (excludedIds != null && !excludedIds.isEmpty()) {
            prompt.append("다음 항목들은 제외해주세요: ").append(String.join(", ", excludedIds)).append("\n");
        }
        prompt.append("\nJSON 형식으로 응답해주세요.");
        return prompt.toString();
    }

    private String callOpenAiApi(String prompt) {
        WebClient webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages",
                Arrays.asList(Map.of("role", "system", "content", "You are a helpful travel assistant for Korea."),
                        Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        return webClient.post().uri("/chat/completions").bodyValue(requestBody).retrieve().bodyToMono(String.class)
                .block();
    }

    private List<Map<String, Object>> parseRecommendations(String response) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choices = rootNode.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();

                int jsonStart = content.indexOf("{");
                int jsonEnd = content.lastIndexOf("}") + 1;

                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonContent = content.substring(jsonStart, jsonEnd);
                    JsonNode recommendationsNode = objectMapper.readTree(jsonContent).path("recommendations");

                    if (recommendationsNode.isArray()) {
                        for (JsonNode rec : recommendationsNode) {
                            Map<String, Object> recMap = new HashMap<>();
                            recMap.put("type", rec.path("type").asText());
                            recMap.put("title", rec.path("title").asText());
                            recMap.put("description", rec.path("description").asText());
                            recMap.put("reason", rec.path("reason").asText());
                            recMap.put("address", rec.path("address").asText("주소 정보 없음"));

                            // Robust Parsing
                            String mapx = null;
                            if (!rec.path("mapx").isMissingNode())
                                mapx = rec.path("mapx").asText();
                            else if (!rec.path("mapX").isMissingNode())
                                mapx = rec.path("mapX").asText();
                            else if (!rec.path("map_x").isMissingNode())
                                mapx = rec.path("map_x").asText();

                            String mapy = null;
                            if (!rec.path("mapy").isMissingNode())
                                mapy = rec.path("mapy").asText();
                            else if (!rec.path("mapY").isMissingNode())
                                mapy = rec.path("mapY").asText();
                            else if (!rec.path("map_y").isMissingNode())
                                mapy = rec.path("map_y").asText();

                            if (mapx == null || mapx.isEmpty() || "null".equals(mapx))
                                mapx = "0.0";
                            if (mapy == null || mapy.isEmpty() || "null".equals(mapy))
                                mapy = "0.0";

                            recMap.put("mapx", mapx);
                            recMap.put("mapy", mapy);
                            recMap.put("mapX", mapx);
                            recMap.put("mapY", mapy);
                            recMap.put("map_x", mapx);
                            recMap.put("map_y", mapy);
                            recMap.put("image_keyword", rec.path("image_keyword").asText("travel"));
                            recommendations.add(recMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recommendations;
    }

    /**
     * 위치 기반 여행지 추천
     */
    public List<Map<String, String>> suggestDestinationsByLocation(double lat, double lon) {
        String prompt = String.format("사용자의 현재 위치는 위도: %f, 경도: %f 입니다.\n" + "이 위치에서 갈 수 있는 대한민국 국내 여행지 3곳을 추천해주세요.\n"
                + "단, 현재 위치와 너무 가까운 곳(같은 시/군/구)은 제외하고, 여행 기분을 낼 수 있는 타 도시로 추천하세요.\n\n" + "응답은 반드시 아래 JSON 형식으로만 주세요:\n"
                + "{\n" + "  \"destinations\": [\n"
                + "    { \"regionName\": \"부산\", \"regionCode\": \"6\", \"reason\": \"이유...\" },\n"
                + "    { \"regionName\": \"강릉\", \"regionCode\": \"32\", \"reason\": \"이유...\" }\n" + "  ]\n" + "}\n\n"
                + "주의: regionCode는 다음 한국관광공사 코드를 엄격히 따르세요.\n" + "서울:1, 인천:2, 대전:3, 대구:4, 광주:5, 부산:6, 울산:7, 세종:8, \n"
                + "경기:31, 강원:32, 충북:33, 충남:34, 경북:35, 경남:36, 전북:37, 전남:38, 제주:39", lat, lon);

        try {
            String response = callOpenAiApi(prompt);
            JsonNode root = objectMapper.readTree(response);

            String content = root.path("choices").get(0).path("message").path("content").asText();
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}") + 1;
            JsonNode dests = objectMapper.readTree(content.substring(start, end)).path("destinations");

            List<Map<String, String>> result = new ArrayList<>();
            if (dests.isArray()) {
                for (JsonNode node : dests) {
                    Map<String, String> map = new HashMap<>();
                    map.put("regionName", node.path("regionName").asText());
                    map.put("regionCode", node.path("regionCode").asText());
                    map.put("reason", node.path("reason").asText());
                    result.add(map);
                }
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(Map.of("regionName", "부산", "regionCode", "6", "reason", "언제 가도 좋은 바다의 도시"),
                    Map.of("regionName", "강릉", "regionCode", "32", "reason", "커피와 바다가 있는 힐링 여행"),
                    Map.of("regionName", "전주", "regionCode", "37", "reason", "맛과 전통이 살아있는 도시"));
        }
    }
}
