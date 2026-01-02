package com.tripboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripboard.entity.CacheTourApi;
import com.tripboard.mapper.CacheTourApiMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KoreanTourApiService {
    private static final Logger logger = LoggerFactory.getLogger(KoreanTourApiService.class);

    @Value("${tour.api.base-url}")
    private String baseUrl;

    @Value("${tour.api.photo-base-url}")
    private String photoBaseUrl;

    @Value("${tour.api.service-key}")
    private String serviceKey;

    /**
     * serviceKey를 처리하여 반환
     */
    private String getDecodedServiceKey() {
        try {
            if (serviceKey.contains("%3D") || serviceKey.contains("%3d")) {
                return URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);
            } else {
                return serviceKey;
            }
        } catch (Exception e) {
            logger.warn("serviceKey 디코딩 실패, 원본 사용: {}", e.getMessage());
            return serviceKey;
        }
    }

    private final CacheTourApiMapper tourApiCacheMapper;
    private final ObjectMapper objectMapper;

    public KoreanTourApiService(CacheTourApiMapper tourApiCacheMapper, ObjectMapper objectMapper) {
        this.tourApiCacheMapper = tourApiCacheMapper;
        this.objectMapper = objectMapper;
    }

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    private WebClient getPhotoWebClient() {
        return WebClient.builder()
                .baseUrl(photoBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 지역 기반 관광지 조회 및 캐싱
     */
    public List<CacheTourApi> fetchAndCacheTouristSpots(String areaCode, Boolean hasPet) {
        try {
            List<CacheTourApi> cached = tourApiCacheMapper.findByAreaCode(areaCode, "12", hasPet, 20);
            if (!cached.isEmpty()) {
                logger.debug("✅ 캐시 히트: 관광지 데이터 ({}개)", cached.size());
                return cached;
            }

            logger.info("📡 관광지 API 요청 시작 (수동 URL): areaCode={}", areaCode);

            String requestUrl = String.format(
                    "%s/areaBasedList2?serviceKey=%s&numOfRows=20&pageNo=1&MobileOS=ETC&MobileApp=TripBoard&_type=json&contentTypeId=12&areaCode=%s",
                    baseUrl, getDecodedServiceKey(), areaCode);

            logger.info("🔗 관광지 요청 URL: {}", requestUrl);

            String response = getWebClient().get()
                    .uri(java.net.URI.create(requestUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<CacheTourApi> results = parseAreaBasedListResponse(response, "12", areaCode);

            logger.info("📦 받아온 관광지 개수: {}", results.size());

            if (!results.isEmpty()) {
                logger.info("🔍 첫 번째 관광지 지역 확인: {} (요청한 지역: {})",
                        results.get(0).getAreaCode(), areaCode);
            }

            for (CacheTourApi cache : results) {
                enrichWithPhotos(cache);

                CacheTourApi existing = tourApiCacheMapper.findByContentId(cache.getContentId());
                if (existing == null) {
                    cache.setCreatedAt(LocalDateTime.now());
                    cache.setUpdatedAt(LocalDateTime.now());
                    tourApiCacheMapper.insert(cache);
                } else {
                    existing.setTitle(cache.getTitle());
                    existing.setAddr1(cache.getAddr1());
                    existing.setAddr2(cache.getAddr2());
                    existing.setMapX(cache.getMapX());
                    existing.setMapY(cache.getMapY());
                    existing.setFirstImage(cache.getFirstImage());
                    existing.setFirstImage2(cache.getFirstImage2());
                    existing.setOverview(cache.getOverview());
                    existing.setIsPetFriendly(cache.getIsPetFriendly());
                    existing.setUpdatedAt(LocalDateTime.now());
                    tourApiCacheMapper.update(existing);
                }
            }

            return results;
        } catch (Exception e) {
            logger.error("관광지 API 호출 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 지역 기반 숙소 조회 및 캐싱
     */
    public List<CacheTourApi> fetchAndCacheAccommodations(String areaCode, Boolean hasPet) {
        try {
            List<CacheTourApi> cached = tourApiCacheMapper.findByAreaCode(areaCode, "32", hasPet, 20);
            if (!cached.isEmpty()) {
                return cached;
            }

            logger.info("📡 숙소 API 요청 시작 (수동 URL): areaCode={}", areaCode);

            String requestUrl = String.format(
                    "%s/areaBasedList2?serviceKey=%s&numOfRows=20&pageNo=1&MobileOS=ETC&MobileApp=TripBoard&_type=json&contentTypeId=32&areaCode=%s",
                    baseUrl, getDecodedServiceKey(), areaCode);

            logger.info("🔗 요청 URL: {}", requestUrl);

            String response = getWebClient().get()
                    .uri(java.net.URI.create(requestUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<CacheTourApi> results = parseAreaBasedListResponse(response, "32", areaCode);

            logger.info("📦 받아온 숙소 개수: {}", results.size());

            if (!results.isEmpty()) {
                logger.info("🔍 첫 번째 숙소 지역 확인: {} (요청한 지역: {})",
                        results.get(0).getAreaCode(), areaCode);
            }

            int savedCount = 0;
            for (CacheTourApi cache : results) {
                CacheTourApi existing = tourApiCacheMapper.findByContentId(cache.getContentId());
                if (existing == null) {
                    cache.setCreatedAt(LocalDateTime.now());
                    cache.setUpdatedAt(LocalDateTime.now());
                    tourApiCacheMapper.insert(cache);
                    savedCount++;
                } else {
                    existing.setTitle(cache.getTitle());
                    existing.setAddr1(cache.getAddr1());
                    existing.setAddr2(cache.getAddr2());
                    existing.setMapX(cache.getMapX());
                    existing.setMapY(cache.getMapY());
                    existing.setFirstImage(cache.getFirstImage());
                    existing.setFirstImage2(cache.getFirstImage2());
                    existing.setOverview(cache.getOverview());
                    existing.setIsPetFriendly(cache.getIsPetFriendly());
                    existing.setUpdatedAt(LocalDateTime.now());
                    tourApiCacheMapper.update(existing);
                }
            }
            logger.info("총 {}개의 숙소 저장 완료", savedCount);

            return results;

        } catch (Exception e) {
            logger.error("숙소 API 호출 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 음식점 정보 조회
     */
    public List<CacheTourApi> fetchAndCacheRestaurants(String areaCode) {
        try {
            List<CacheTourApi> cached = tourApiCacheMapper.findByAreaCode(areaCode, "39", null, 10);
            if (!cached.isEmpty()) {
                return cached;
            }
            String decodedKey = getDecodedServiceKey();
            String response = getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/areaBasedList2")
                            .queryParam("serviceKey", decodedKey)
                            .queryParam("numOfRows", 10)
                            .queryParam("pageNo", 1)
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "TripBoard")
                            .queryParam("_type", "json")
                            .queryParam("areaCode", areaCode)
                            .queryParam("contentTypeId", "39")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<CacheTourApi> results = parseAreaBasedListResponse(response, "39", areaCode);

            for (CacheTourApi cache : results) {
                CacheTourApi existing = tourApiCacheMapper.findByContentId(cache.getContentId());
                if (existing == null) {
                    cache.setCreatedAt(LocalDateTime.now());
                    cache.setUpdatedAt(LocalDateTime.now());
                    tourApiCacheMapper.insert(cache);
                    logger.debug("새 음식점 저장: {} (contentId={})", cache.getTitle(), cache.getContentId());
                }
            }

            return results;
        } catch (Exception e) {
            logger.error("fetchAndCacheRestaurants 예외: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 키워드 기반 검색
     */
    public List<CacheTourApi> searchByKeyword(String keyword, String contentTypeId) {
        List<CacheTourApi> cachedResults = tourApiCacheMapper.searchByKeyword(keyword, contentTypeId, 20);

        try {
            logger.info("📡 검색 API 요청 시작: keyword={}, type={}", keyword, contentTypeId);

            String decodedKey = getDecodedServiceKey();
            String response = getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService1/searchKeyword1")
                            .queryParam("serviceKey", decodedKey)
                            .queryParam("numOfRows", 10)
                            .queryParam("pageNo", 1)
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "TripBoard")
                            .queryParam("_type", "json")
                            .queryParam("listYN", "Y")
                            .queryParam("arrange", "A")
                            .queryParam("keyword", keyword)
                            .queryParam("contentTypeId", contentTypeId)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<CacheTourApi> apiResults = parseKeywordSearchResponse(response, contentTypeId);

            if (!apiResults.isEmpty()) {
                logger.info("📦 검색 API 결과: {}건", apiResults.size());
                for (CacheTourApi item : apiResults) {
                    try {
                        CacheTourApi existing = tourApiCacheMapper.findByContentId(item.getContentId());
                        if (existing == null) {
                            item.setCreatedAt(LocalDateTime.now());
                            item.setUpdatedAt(LocalDateTime.now());
                            tourApiCacheMapper.insert(item);
                        } else {
                            existing.setUpdatedAt(LocalDateTime.now());
                            tourApiCacheMapper.update(existing);
                        }
                    } catch (Exception e) {
                        // Duplicate key ignore
                    }
                }
                return apiResults;
            } else {
                logger.info("검색 API 결과 없음");
            }

        } catch (Exception e) {
            logger.error("Search API Failed: {}", e.getMessage());
        }

        logger.info("캐시된 검색 결과 반환: {}건", cachedResults.size());
        return cachedResults;
    }

    /**
     * 키워드 검색 응답 파싱
     */
    private List<CacheTourApi> parseKeywordSearchResponse(String response, String contentTypeId) {
        List<CacheTourApi> results = new ArrayList<>();

        try {
            if (response == null || response.isEmpty()) {
                logger.warn("응답이 비어있습니다");
                return results;
            }

            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode responseNode = rootNode.path("response");
            JsonNode headerNode = responseNode.path("header");
            String resultCode = headerNode.path("resultCode").asText();
            String resultMsg = headerNode.path("resultMsg").asText();

            logger.debug("API 응답 코드: {} 메시지: {}", resultCode, resultMsg);

            if (!"0000".equals(resultCode)) {
                logger.warn("API 오류 발생: {}", resultMsg);
                return results;
            }

            JsonNode bodyNode = responseNode.path("body");
            String totalCount = bodyNode.path("totalCount").asText();
            logger.debug("전체 결과 수: {}", totalCount);

            JsonNode itemsNode = bodyNode.path("items");
            JsonNode itemNode = itemsNode.path("item");

            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    CacheTourApi cache = buildTourApiCacheFromItem(item, contentTypeId);
                    if (cache != null && cache.getContentId() != null && !cache.getContentId().isEmpty()) {
                        results.add(cache);
                    }
                }
            } else if (itemNode.isObject() && !itemNode.isNull() && !itemNode.isMissingNode()) {
                CacheTourApi cache = buildTourApiCacheFromItem(itemNode, contentTypeId);
                if (cache != null && cache.getContentId() != null && !cache.getContentId().isEmpty()) {
                    results.add(cache);
                }
            } else {
                logger.debug("검색 결과가 없습니다");
            }

            logger.debug("파싱 완료, 결과 수: {}", results.size());
        } catch (Exception e) {
            logger.error("응답 파싱 오류: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * JSON 항목에서 TourApiCache 객체 생성
     */
    private CacheTourApi buildTourApiCacheFromItem(JsonNode item, String contentTypeId) {
        try {
            String contentId = item.path("contentid").asText();
            if (contentId == null || contentId.isEmpty()) {
                return null;
            }

            return CacheTourApi.builder()
                    .contentId(contentId)
                    .contentTypeId(contentTypeId)
                    .title(item.path("title").asText(""))
                    .areaCode(item.path("areacode").asText(""))
                    .sigunguCode(item.path("sigungucode").asText(""))
                    .cat1(item.path("cat1").asText(""))
                    .cat2(item.path("cat2").asText(""))
                    .cat3(item.path("cat3").asText(""))
                    .addr1(item.path("addr1").asText(""))
                    .addr2(item.path("addr2").asText(""))
                    .mapX(item.path("mapx").asText(""))
                    .mapY(item.path("mapy").asText(""))
                    .firstImage(item.path("firstimage").asText(""))
                    .firstImage2(item.path("firstimage2").asText(""))
                    .overview(item.path("overview").asText(""))
                    .isPetFriendly(false)
                    .build();
        } catch (Exception e) {
            logger.error("TourApiCache 생성 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * API 응답 파싱
     */
    private List<CacheTourApi> parseAreaBasedListResponse(String response, String contentTypeId, String areaCode) {
        List<CacheTourApi> results = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode items = rootNode.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    CacheTourApi cache = CacheTourApi.builder()
                            .contentId(item.path("contentid").asText())
                            .contentTypeId(contentTypeId)
                            .title(item.path("title").asText())
                            .areaCode(item.path("areacode").asText(areaCode))
                            .sigunguCode(item.path("sigungucode").asText())
                            .cat1(item.path("cat1").asText())
                            .cat2(item.path("cat2").asText())
                            .cat3(item.path("cat3").asText())
                            .addr1(item.path("addr1").asText())
                            .addr2(item.path("addr2").asText())
                            .mapX(item.path("mapx").asText())
                            .mapY(item.path("mapy").asText())
                            .firstImage(item.path("firstimage").asText())
                            .firstImage2(item.path("firstimage2").asText())
                            .overview(item.path("overview").asText())
                            .isPetFriendly(false)
                            .build();

                    results.add(cache);
                }
            } else if (items.isObject()) {
                JsonNode item = items;
                CacheTourApi cache = CacheTourApi.builder()
                        .contentId(item.path("contentid").asText())
                        .contentTypeId(contentTypeId)
                        .title(item.path("title").asText())
                        .areaCode(item.path("areacode").asText(areaCode))
                        .sigunguCode(item.path("sigungucode").asText())
                        .cat1(item.path("cat1").asText())
                        .cat2(item.path("cat2").asText())
                        .cat3(item.path("cat3").asText())
                        .addr1(item.path("addr1").asText())
                        .addr2(item.path("addr2").asText())
                        .mapX(item.path("mapx").asText())
                        .mapY(item.path("mapy").asText())
                        .firstImage(item.path("firstimage").asText())
                        .firstImage2(item.path("firstimage2").asText())
                        .overview(item.path("overview").asText())
                        .isPetFriendly(false)
                        .build();

                results.add(cache);
            }
        } catch (Exception e) {
            logger.error("parseAreaBasedListResponse 예외: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 관광사진 정보 API를 사용하여 이미지 정보 보강
     */
    private void enrichWithPhotos(CacheTourApi cache) {
        if (cache.getContentId() == null || cache.getContentId().isEmpty()) {
            return;
        }

        try {
            String decodedKey = getDecodedServiceKey();
            String response = getPhotoWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/galleryList1")
                            .queryParam("serviceKey", decodedKey)
                            .queryParam("numOfRows", 5)
                            .queryParam("pageNo", 1)
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "TripBoard")
                            .queryParam("_type", "json")
                            .queryParam("contentId", cache.getContentId())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            parsePhotoResponse(response, cache);
        } catch (Exception e) {
            logger.debug("enrichWithPhotos 예외 (무시됨): {}", e.getMessage(), e);
        }
    }

    /**
     * 관광사진 정보 API 응답 파싱
     */
    private void parsePhotoResponse(String response, CacheTourApi cache) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode items = rootNode.path("response").path("body").path("items").path("item");

            if (items.isArray() && items.size() > 0) {
                JsonNode firstPhoto = items.get(0);
                String imageUrl = firstPhoto.path("galWebImageUrl").asText();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (cache.getFirstImage() == null || cache.getFirstImage().isEmpty()) {
                        cache.setFirstImage(imageUrl);
                    }
                }

                if (items.size() > 1) {
                    JsonNode secondPhoto = items.get(1);
                    String imageUrl2 = secondPhoto.path("galWebImageUrl").asText();
                    if (imageUrl2 != null && !imageUrl2.isEmpty()) {
                        if (cache.getFirstImage2() == null || cache.getFirstImage2().isEmpty()) {
                            cache.setFirstImage2(imageUrl2);
                        }
                    }
                }
            } else if (items.isObject()) {
                String imageUrl = items.path("galWebImageUrl").asText();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (cache.getFirstImage() == null || cache.getFirstImage().isEmpty()) {
                        cache.setFirstImage(imageUrl);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("parsePhotoResponse 예외: {}", e.getMessage(), e);
        }
    }
}
