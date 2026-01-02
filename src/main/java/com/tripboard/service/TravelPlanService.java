package com.tripboard.service;

import com.tripboard.dto.TravelPlanRequest;
import com.tripboard.entity.*;
import com.tripboard.mapper.*;
import com.tripboard.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TravelPlanService {
    private static final Logger logger = LoggerFactory.getLogger(TravelPlanService.class);

    private final TravelPlanMapper travelPlanMapper;
    private final CacheTourApiMapper tourApiCacheMapper;
    private final CacheRailwayStationMapper cacheRailwayStationMapper;
    private final PlanSpotMapper planSpotMapper;
    private final PlanAccommodationMapper planAccommodationMapper;
    private final PlanTransportMapper planTransportMapper;
    private final KoreanTourApiService koreanTourApiService;
    private final OpenAiService openAiService;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final KorailApiService korailApiService;

    /**
     * 여행 계획 생성
     */
    public TravelPlan createPlan(Long userId, TravelPlanRequest request) {
        TravelPlan plan = TravelPlan.builder()
                .userId(userId)
                .title(request.getTitle() != null ? request.getTitle() : "새 여행 계획")
                .departureRegionCode(request.getDepartureRegionCode())
                .arrivalRegionCode(request.getArrivalRegionCode())
                .departureDate(request.getDepartureDate())
                .arrivalDate(request.getArrivalDate())
                .adultCount(request.getAdultCount() != null ? request.getAdultCount() : 1)
                .childCount(request.getChildCount() != null ? request.getChildCount() : 0)
                .hasPet(request.getHasPet() != null ? request.getHasPet() : false)
                .templateId(request.getTemplateId())
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        travelPlanMapper.insert(plan);

        if (request.getArrivalRegionCode() != null && request.getDepartureDate() != null) {
            try {
                generateRecommendations(plan);
            } catch (Exception e) {
                logger.warn("generateRecommendations 실패: {}", e.getMessage(), e);
            }
        }

        return plan;
    }

    /**
     * 빈 계획 생성
     */
    @Transactional
    public TravelPlan createEmptyPlan(Long userId) {
        TravelPlan plan = TravelPlan.builder()
                .userId(userId)
                .title("새 여행 계획")
                .departureRegionCode(null)
                .arrivalRegionCode(null)
                .departureDate(null)
                .arrivalDate(null)
                .adultCount(1)
                .childCount(0)
                .hasPet(false)
                .templateId(null)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        travelPlanMapper.insert(plan);
        return plan;
    }

    private void generateRecommendations(TravelPlan plan) {
        logger.info(">>> 병렬 처리 시작: 모든 API를 동시에 호출합니다.");
        long startTime = System.currentTimeMillis();

        CompletableFuture<List<CacheTourApi>> destinationFuture = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Thread: 관광지 조회 시작");
                List<CacheTourApi> destinations = tourApiCacheMapper.findByAreaCode(
                        plan.getArrivalRegionCode(), "12", plan.getHasPet(), 50);
                if (destinations.isEmpty()) {
                    destinations = koreanTourApiService.fetchAndCacheTouristSpots(
                            plan.getArrivalRegionCode(), plan.getHasPet());
                }
                logger.info("Thread: 관광지 조회 완료 ({}건)", destinations.size());
                return destinations;
            } catch (Exception e) {
                logger.error("관광지 처리 중 오류", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<CacheTourApi>> accommodationFuture = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Thread: 숙소 조회 시작");
                List<CacheTourApi> accommodations = tourApiCacheMapper.findByAreaCode(
                        plan.getArrivalRegionCode(), "32", plan.getHasPet(), 5);
                if (accommodations.isEmpty()) {
                    accommodations = koreanTourApiService.fetchAndCacheAccommodations(
                            plan.getArrivalRegionCode(), plan.getHasPet());
                }
                logger.info("Thread: 숙소 조회 완료 ({}건)", accommodations.size());
                return accommodations;
            } catch (Exception e) {
                logger.error("숙소 처리 중 오류", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<Void> aiFuture = CompletableFuture.runAsync(() -> {
            try {
                logger.info("Thread: OpenAI 추천 요청 시작");
                String regionName = getRegionName(plan.getArrivalRegionCode());
                String depDate = plan.getDepartureDate() != null
                        ? plan.getDepartureDate().format(DateTimeFormatter.ISO_DATE)
                        : "";
                String arrDate = plan.getArrivalDate() != null
                        ? plan.getArrivalDate().format(DateTimeFormatter.ISO_DATE)
                        : "";

                List<Map<String, Object>> aiRecommendations = openAiService.generateTravelRecommendations(
                        regionName, depDate, arrDate,
                        plan.getAdultCount(), plan.getChildCount(), plan.getHasPet());

                for (Map<String, Object> aiRec : aiRecommendations) {
                    AiRecommendation recommendation = AiRecommendation.builder()
                            .planId(plan.getPlanId())
                            .userId(plan.getUserId())
                            .recommendationType((String) aiRec.get("type"))
                            .title((String) aiRec.get("title"))
                            .description((String) aiRec.get("description"))
                            .reason((String) aiRec.get("reason"))
                            .address((String) aiRec.getOrDefault("address", "위치 정보 없음"))
                            .mapX(aiRec.get("mapx") != null ? (String) aiRec.get("mapx") : (String) aiRec.get("mapX"))
                            .mapY(aiRec.get("mapy") != null ? (String) aiRec.get("mapy") : (String) aiRec.get("mapY"))
                            .imageKeyword((String) aiRec.getOrDefault("image_keyword", "travel"))
                            .createdAt(LocalDateTime.now())
                            .build();

                    aiRecommendationMapper.insert(recommendation);
                }
                logger.info("Thread: OpenAI 추천 저장 완료");
            } catch (Exception e) {
                logger.error(
                        "OpenAI 추천 데이터 저장 실패. DB 테이블(ai_recommendations)에 'address', 'image_keyword' 컬럼이 있는지 확인해주세요.",
                        e);
            }
        });

        CompletableFuture<List<Map<String, Object>>> trainFuture = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> trains = Collections.emptyList();
            try {
                if (plan.getDepartureRegionCode() != null && plan.getArrivalRegionCode() != null) {
                    logger.info("Thread: 코레일 조회 시작");
                    String depStation = korailApiService.getStationNameByRegionCode(plan.getDepartureRegionCode());
                    String arrStation = korailApiService.getStationNameByRegionCode(plan.getArrivalRegionCode());

                    if (!"제주".equals(depStation) && !"제주".equals(arrStation)) {
                        LocalDate depDate = plan.getDepartureDate() != null ? plan.getDepartureDate() : LocalDate.now();
                        LocalDate arrDate = plan.getArrivalDate() != null ? plan.getArrivalDate() : depDate;

                        List<Map<String, Object>> outbound = korailApiService.searchTrains(depStation, arrStation,
                                depDate);

                        List<Map<String, Object>> inbound = korailApiService.searchTrains(arrStation, depStation,
                                arrDate);

                        List<Map<String, Object>> merged = new ArrayList<>();
                        if (outbound != null)
                            merged.addAll(outbound);
                        if (inbound != null)
                            merged.addAll(inbound);

                        trains = merged;
                    }

                }
                logger.info("Thread: 코레일 조회 완료 ({}건)", trains.size());
                return trains;
            } catch (Exception e) {
                logger.error("코레일 API 처리 중 오류", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture.allOf(destinationFuture, accommodationFuture, aiFuture, trainFuture).join();

        try

        {
            generatePlanSchedule(
                    plan,
                    destinationFuture.get(),
                    accommodationFuture.get(),
                    trainFuture.get());

        } catch (Exception e) {
            logger.error("일정 분배 중 오류 발생: {}", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        logger.info(">>> 전체 추천 로직 완료! 소요 시간: {}ms", (endTime - startTime));
    }

    private LocalDateTime parseTimeToDateTime(LocalDate date, String time) {
        try {
            if (time == null || time.isEmpty()) {
                return null;
            }

            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return date.atTime(hour, minute);
        } catch (Exception e) {
            logger.warn("시간 파싱 실패: {}", time);
            return null;
        }
    }

    private String getRegionName(String regionCode) {
        Map<String, String> regionMap = new HashMap<>();

        regionMap.put("1", "서울");
        regionMap.put("2", "인천");
        regionMap.put("3", "대전");
        regionMap.put("4", "대구");
        regionMap.put("5", "광주");
        regionMap.put("6", "부산");
        regionMap.put("7", "울산");
        regionMap.put("8", "세종");

        regionMap.put("31", "경기도");
        regionMap.put("32", "강원도");
        regionMap.put("33", "충청북도");
        regionMap.put("34", "충청남도");
        regionMap.put("35", "경상북도");
        regionMap.put("36", "경상남도");
        regionMap.put("37", "전라북도");
        regionMap.put("38", "전라남도");
        regionMap.put("39", "제주도");

        return regionMap.getOrDefault(regionCode, "한국");
    }

    /**
     * ID로 여행 계획 조회
     */
    public TravelPlan getPlan(Long planId) {
        return travelPlanMapper.findById(planId);
    }

    /**
     * 사용자별 여행 계획 조회
     */
    public List<TravelPlan> getUserPlans(Long userId) {
        if (userId == null) {
            System.err.println("getUserPlans: userId가 null입니다");
            throw new IllegalArgumentException("사용자 ID가 null입니다");
        }
        try {
            System.out.println("MyBatis 쿼리 실행 시작 - userId: " + userId);
            List<TravelPlan> plans = travelPlanMapper.findByUserId(userId);
            System.out.println("MyBatis 쿼리 실행 완료 - 결과: " + (plans != null ? plans.size() + "개" : "null"));
            return plans != null ? plans : new java.util.ArrayList<>();
        } catch (org.apache.ibatis.exceptions.PersistenceException e) {
            logger.error("MyBatis PersistenceException 발생: {}", e.getMessage(), e);
            throw new RuntimeException("데이터베이스 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("getUserPlans 예외 발생: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("여행 계획 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 여행 계획 상세 정보 조회
     */
    public Map<String, Object> getPlanDetails(Long planId) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        Map<String, Object> details = new HashMap<>();
        details.put("plan", plan);
        details.put("transportations", planTransportMapper.findByPlanId(planId));
        details.put("aiRecommendations", aiRecommendationMapper.findByPlanId(planId));

        List<PlanSpot> spots = planSpotMapper.findByPlanId(planId);
        List<Map<String, Object>> spotDetails = new ArrayList<>();
        for (PlanSpot spot : spots) {
            if (spot.getContentId() != null) {
                CacheTourApi cache = tourApiCacheMapper.findByContentId(spot.getContentId());

                if (cache != null) {
                    Map<String, Object> detail = new HashMap<>();

                    detail.put("planDestination", spot);
                    detail.put("planSpotId", spot.getSpotOrder());

                    detail.put("tourInfo", cache);
                    spotDetails.add(detail);
                } else {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("planDestination", spot);
                    detail.put("planSpotId", spot.getSpotOrder());

                    Map<String, Object> tourInfo = new HashMap<>();
                    tourInfo.put("contentId", spot.getContentId());
                    tourInfo.put("title", "알 수 없는 장소 (" + spot.getContentId() + ")");
                    detail.put("tourInfo", tourInfo);

                    spotDetails.add(detail);
                }
            }
        }
        details.put("destinationDetails", spotDetails);
        details.put("destinations", spots);

        List<PlanAccommodation> accs = planAccommodationMapper.findByPlanId(planId);
        List<Map<String, Object>> accDetails = new ArrayList<>();
        for (PlanAccommodation acc : accs) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("planId", acc.getPlanId());
            detail.put("checkInDate", acc.getCheckInDate());
            detail.put("contentId", acc.getContentId());
            detail.put("accommodationOrder", acc.getAccommodationOrder());
            detail.put("checkOutDate", acc.getCheckOutDate());
            detail.put("isSelected", acc.getIsSelected());

            if (acc.getContentId() != null) {
                CacheTourApi cache = tourApiCacheMapper.findByContentId(acc.getContentId());
                if (cache != null) {
                    Map<String, Object> tourInfo = new HashMap<>();
                    tourInfo.put("contentId", cache.getContentId());
                    tourInfo.put("title", cache.getTitle());
                    tourInfo.put("firstImage", cache.getFirstImage());
                    tourInfo.put("firstImage2", cache.getFirstImage2());
                    tourInfo.put("addr1", cache.getAddr1());
                    tourInfo.put("addr2", cache.getAddr2());
                    tourInfo.put("mapx", cache.getMapX());
                    tourInfo.put("mapy", cache.getMapY());
                    tourInfo.put("mapX", cache.getMapX());
                    tourInfo.put("mapY", cache.getMapY());
                    tourInfo.put("overview", cache.getOverview());
                    tourInfo.put("contentTypeId", cache.getContentTypeId());

                    detail.put("tourInfo", tourInfo);
                } else {
                    Map<String, Object> tourInfo = new HashMap<>();
                    tourInfo.put("contentId", acc.getContentId());
                    tourInfo.put("title", "숙소 (" + acc.getContentId() + ")");
                    detail.put("tourInfo", tourInfo);
                }
            }

            accDetails.add(detail);
        }
        details.put("accommodations", accDetails);

        return details;
    }

    /**
     * 여행 계획 복사 (공유용 스냅샷)
     */
    @Transactional
    public TravelPlan copyPlanForShare(Long originalPlanId) {
        // 원본 계획 상세 정보 가져오기
        Map<String, Object> originalDetails = getPlanDetails(originalPlanId);
        TravelPlan originalPlan = (TravelPlan) originalDetails.get("plan");

        if (originalPlan == null) {
            throw new RuntimeException("원본 여행 계획을 찾을 수 없습니다");
        }

        // 새로운 계획 생성 (공유용)
        TravelPlan newPlan = TravelPlan.builder()
                .userId(originalPlan.getUserId())
                .title(originalPlan.getTitle() + " (공유본)")
                .departureRegionCode(originalPlan.getDepartureRegionCode())
                .arrivalRegionCode(originalPlan.getArrivalRegionCode())
                .departureDate(originalPlan.getDepartureDate())
                .arrivalDate(originalPlan.getArrivalDate())
                .adultCount(originalPlan.getAdultCount())
                .childCount(originalPlan.getChildCount())
                .hasPet(originalPlan.getHasPet())
                .templateId(originalPlan.getTemplateId())
                .isPublic(false) // 공유본은 비공개
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        travelPlanMapper.insert(newPlan);
        Long newPlanId = newPlan.getPlanId();

        // 관광지 복사
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> destinationDetails = (List<Map<String, Object>>) originalDetails
                .get("destinationDetails");
        if (destinationDetails != null) {
            for (Map<String, Object> destDetail : destinationDetails) {
                PlanSpot originalSpot = (PlanSpot) destDetail.get("planDestination");
                if (originalSpot != null) {
                    PlanSpot newSpot = PlanSpot.builder()
                            .planId(newPlanId)
                            .contentId(originalSpot.getContentId())
                            .visitDate(originalSpot.getVisitDate())
                            .visitTime(originalSpot.getVisitTime())
                            .duration(originalSpot.getDuration())
                            .spotOrder(originalSpot.getSpotOrder())
                            .isSelected(originalSpot.getIsSelected())
                            .createdAt(LocalDateTime.now())
                            .build();
                    planSpotMapper.insert(newSpot);
                }
            }
        }

        // 숙소 복사
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> accommodations = (List<Map<String, Object>>) originalDetails.get("accommodations");
        if (accommodations != null) {
            for (Map<String, Object> accDetail : accommodations) {
                PlanAccommodation newAcc = PlanAccommodation.builder()
                        .planId(newPlanId)
                        .contentId((String) accDetail.get("contentId"))
                        .checkInDate((LocalDate) accDetail.get("checkInDate"))
                        .checkOutDate((LocalDate) accDetail.get("checkOutDate"))
                        .accommodationOrder((Integer) accDetail.get("accommodationOrder"))
                        .isSelected((Boolean) accDetail.get("isSelected"))
                        .createdAt(LocalDateTime.now())
                        .build();
                planAccommodationMapper.insert(newAcc);
            }
        }

        // 교통편 복사
        @SuppressWarnings("unchecked")
        List<PlanTransport> transportations = (List<PlanTransport>) originalDetails.get("transportations");
        if (transportations != null) {
            for (PlanTransport originalTransport : transportations) {
                PlanTransport newTransport = PlanTransport.builder()
                        .planId(newPlanId)
                        .transportType(originalTransport.getTransportType())
                        .departureStationCode(originalTransport.getDepartureStationCode())
                        .arrivalStationCode(originalTransport.getArrivalStationCode())
                        .departureTime(originalTransport.getDepartureTime())
                        .arrivalTime(originalTransport.getArrivalTime())
                        .price(originalTransport.getPrice())
                        .reservationUrl(originalTransport.getReservationUrl())
                        .transportOrder(originalTransport.getTransportOrder())
                        .isSelected(originalTransport.getIsSelected())
                        .createdAt(LocalDateTime.now())
                        .build();
                planTransportMapper.insert(newTransport);
            }
        }

        return newPlan;
    }

    /**
     * 여행 계획 수정 (제목, 날짜 등)
     */
    @Transactional
    public TravelPlan updatePlan(Long planId, TravelPlanRequest request) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        // 제목 수정
        if (request.getTitle() != null) {
            plan.setTitle(request.getTitle());
        }

        // 날짜 수정 (출발일/도착일이 변경되면 기간 유효성 체크 필요할 수 있음)
        if (request.getDepartureDate() != null) {
            plan.setDepartureDate(request.getDepartureDate());
        }
        if (request.getArrivalDate() != null) {
            plan.setArrivalDate(request.getArrivalDate());
        }

        // 지역 수정
        if (request.getDepartureRegionCode() != null) {
            plan.setDepartureRegionCode(request.getDepartureRegionCode());
        }
        if (request.getArrivalRegionCode() != null) {
            plan.setArrivalRegionCode(request.getArrivalRegionCode());
        }

        // 인원 및 기타 정보 수정
        if (request.getAdultCount() != null)
            plan.setAdultCount(request.getAdultCount());
        if (request.getChildCount() != null)
            plan.setChildCount(request.getChildCount());
        if (request.getHasPet() != null)
            plan.setHasPet(request.getHasPet());

        plan.setUpdatedAt(LocalDateTime.now());

        travelPlanMapper.update(plan);
        return plan;
    }

    @Transactional
    public void deletePlan(Long planId) {
        travelPlanMapper.delete(planId);
    }

    @Transactional
    public void deleteSpot(Long planId, int spotOrder) {
        planSpotMapper.deleteSpot(planId, spotOrder);
    }

    @Transactional
    public void deleteAccommodation(Long planId, LocalDate checkInDate, int accommodationOrder) {
        planAccommodationMapper.deleteAccommodation(planId, checkInDate, accommodationOrder);
    }

    @Transactional
    public void deleteTransport(Long planId, int transportOrder) {
        planTransportMapper.deleteTransport(planId, transportOrder);
    }

    /**
     * 추가 추천 항목 조회
     */
    public List<Map<String, Object>> getAdditionalRecommendations(Long planId, String category) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        String regionName = getRegionName(plan.getArrivalRegionCode());

        List<String> excludedIds = new ArrayList<>();
        if ("destination".equals(category)) {
            List<PlanSpot> dests = planSpotMapper.findByPlanId(planId);
            for (PlanSpot dest : dests) {
                if (dest.getContentId() != null) {
                    excludedIds.add(dest.getContentId());
                }
            }
        }

        List<Map<String, Object>> recommendations = openAiService.getAdditionalRecommendations(
                regionName, category, excludedIds);

        return recommendations;
    }

    /**
     * 데이터셋에서 키워드 검색
     */
    public List<CacheTourApi> searchDataset(String keyword, String category) {
        String contentTypeId = "12";
        if ("accommodation".equals(category)) {
            contentTypeId = "32";
        } else if ("restaurant".equals(category)) {
            contentTypeId = "39";
        }

        List<CacheTourApi> results = koreanTourApiService.searchByKeyword(keyword, contentTypeId);

        if (results.isEmpty()) {
            String areaCode = getAreaCodeFromKeyword(keyword);
            if (areaCode != null) {
                try {
                    System.out.println("지역 기반 검색 시도: " + keyword + " -> areaCode: " + areaCode);
                    if ("destination".equals(category)) {
                        results = koreanTourApiService.fetchAndCacheTouristSpots(areaCode, false);
                    } else if ("accommodation".equals(category)) {
                        results = koreanTourApiService.fetchAndCacheAccommodations(areaCode, false);
                    }

                    if (!results.isEmpty() && !isRegionKeyword(keyword)) {
                        List<CacheTourApi> filtered = results.stream()
                                .filter(item -> (item.getTitle() != null &&
                                        (item.getTitle().contains(keyword) ||
                                                item.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                                        ||
                                        (item.getAddr1() != null &&
                                                (item.getAddr1().contains(keyword) ||
                                                        item.getAddr1().toLowerCase().contains(keyword.toLowerCase())))
                                        ||
                                        (item.getOverview() != null &&
                                                (item.getOverview().contains(keyword) ||
                                                        item.getOverview().toLowerCase()
                                                                .contains(keyword.toLowerCase()))))
                                .limit(20)
                                .collect(java.util.stream.Collectors.toList());

                        if (!filtered.isEmpty()) {
                            results = filtered;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ 지역 기반 검색 실패: " + e.getMessage());
                }
            } else {
                System.out.println("키워드 '" + keyword + "'는 지역명으로 인식되지 않았습니다.");
            }
        }

        return results;
    }

    /**
     * 관광지/숙소 검색 (지역 필터 적용)
     */
    /**
     * 관광지/숙소 검색 (지역 필터 + 지역명 검색 시 추천 적용)
     */
    public List<Map<String, Object>> searchTourWithFilter(String keyword, String contentTypeId, String areaCode) {
        List<CacheTourApi> results = new ArrayList<>();

        // 1. 키워드가 지역명인지 확인 (예: "대전", "제주도" 등)
        String detectedAreaCode = getAreaCodeFromKeyword(keyword);

        // 2. 지역명이면 해당 지역의 인기 항목(추천)을 우선 검색
        if (detectedAreaCode != null) {
            try {
                // 숙소 검색인 경우
                if ("32".equals(contentTypeId)) {
                    // 5개 정도 추천 (개수는 조절 가능)
                    List<CacheTourApi> recommendations = koreanTourApiService
                            .fetchAndCacheAccommodations(detectedAreaCode, false);
                    if (recommendations != null) {
                        results.addAll(recommendations);
                    }
                }
                // 관광지 검색인 경우 (기본값)
                else {
                    // 20개 정도 추천
                    List<CacheTourApi> recommendations = koreanTourApiService
                            .fetchAndCacheTouristSpots(detectedAreaCode, false);
                    if (recommendations != null) {
                        results.addAll(recommendations);
                    }
                }
            } catch (Exception e) {
                logger.warn("지역 추천 조회 중 오류: {}", e.getMessage());
            }
        }

        // 3. 일반 키워드 검색 수행 (지역명이 아니거나, 지역명이어도 추가 결과를 위해)
        // 지역명으로만 검색했을 때 결과가 너무 많거나 적을 수 있으므로 병합
        List<CacheTourApi> keywordResults = koreanTourApiService.searchByKeyword(keyword, contentTypeId);
        if (keywordResults != null) {
            results.addAll(keywordResults);
        }

        // 4. 중복 제거 (contentId 기준)
        List<CacheTourApi> uniqueResults = results.stream()
                .filter(distinctByKey(CacheTourApi::getContentId))
                .limit(50) // 너무 많으면 자름
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> list = new ArrayList<>();
        for (CacheTourApi cache : uniqueResults) {
            if (areaCode != null && !areaCode.isEmpty() && !areaCode.equals(cache.getAreaCode())) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("type", "tour");
            map.put("contentId", cache.getContentId());
            map.put("title", cache.getTitle());
            map.put("addr", cache.getAddr1());
            map.put("image", cache.getFirstImage());
            map.put("contentTypeId", cache.getContentTypeId());
            // mapx, mapy 추가
            map.put("mapx", cache.getMapX());
            map.put("mapy", cache.getMapY());
            map.put("firstimage", cache.getFirstImage()); // frontend expects lowercase sometimes?
            map.put("firstimage2", cache.getFirstImage2());
            map.put("addr1", cache.getAddr1());
            map.put("addr2", cache.getAddr2());

            list.add(map);
        }
        return list;
    }

    // Stream distinct helper
    public static <T> java.util.function.Predicate<T> distinctByKey(
            java.util.function.Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new java.util.concurrent.ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 기차 검색 (시간/종류 필터 적용)
     */
    public List<Map<String, Object>> searchTrainWithFilter(String dep, String arr, String dateStr, String time,
            String trainType) {
        String cleanDate = dateStr;
        if (cleanDate != null && cleanDate.contains("T")) {
            cleanDate = cleanDate.split("T")[0];
        }
        LocalDate date = LocalDate.parse(cleanDate);
        List<Map<String, Object>> allTrains = korailApiService.searchTrains(dep, arr, date);

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> train : allTrains) {
            String tType = (String) train.get("trainType");
            String tTime = (String) train.get("departureTime");

            if (trainType != null && !trainType.isEmpty() && !tType.contains(trainType))
                continue;

            if (time != null && !time.isEmpty() && tTime.compareTo(time) < 0)
                continue;

            train.put("type", "train");
            filtered.add(train);
        }
        return filtered;
    }

    /**
     * 항목 수동 추가
     */
    @Transactional
    public void addManualItem(Long planId, Map<String, Object> itemData) {
        String type = (String) itemData.get("type");

        if ("tour".equals(type)) {
            String contentId = (String) itemData.get("contentId");
            String contentTypeId = (String) itemData.get("contentTypeId");

            String targetDateStr = (String) itemData.get("targetDate");
            LocalDate targetDate = null;
            if (targetDateStr != null) {
                try {
                    targetDate = LocalDate.parse(targetDateStr);
                } catch (Exception e) {
                }
            }

            if ("12".equals(contentTypeId)) {
                int nextOrder = 1;
                if (targetDate != null) {
                    List<PlanSpot> existing = planSpotMapper.findByPlanId(planId);
                    nextOrder = existing.stream().mapToInt(PlanSpot::getSpotOrder).max().orElse(0) + 1;
                }

                PlanSpot dest = PlanSpot.builder()
                        .planId(planId)
                        .contentId(contentId)
                        .visitDate(targetDate)
                        .spotOrder(nextOrder)
                        .isSelected(true)
                        .createdAt(LocalDateTime.now()).build();
                planSpotMapper.insert(dest);
            } else if ("32".equals(contentTypeId)) {
                int nextOrder = 1;
                if (targetDate != null) {
                    List<PlanAccommodation> existing = planAccommodationMapper.findByPlanId(planId);
                    nextOrder = existing.stream().mapToInt(PlanAccommodation::getAccommodationOrder).max().orElse(0)
                            + 1;
                }

                PlanAccommodation acc = PlanAccommodation.builder()
                        .planId(planId)
                        .contentId(contentId)
                        .checkInDate(targetDate)
                        .checkOutDate(targetDate != null ? targetDate.plusDays(1) : null)
                        .accommodationOrder(nextOrder)
                        .isSelected(true)
                        .createdAt(LocalDateTime.now()).build();
                planAccommodationMapper.insert(acc);
            }
        } else if ("train".equals(type)) {
            int fare = 0;
            Object fareObj = itemData.get("fare");
            if (fareObj != null) {
                try {
                    fare = Integer.parseInt(String.valueOf(fareObj));
                } catch (Exception e) {
                    fare = 0;
                }
            }
            PlanTransport info = PlanTransport.builder()
                    .planId(planId)
                    .transportType((String) itemData.get("trainType"))
                    .departureStationCode((String) itemData.get("departureStation"))
                    .arrivalStationCode((String) itemData.get("arrivalStation"))
                    .departureTime(parseTimeToDateTime(LocalDate.now(), (String) itemData.get("departureTime")))
                    .arrivalTime(parseTimeToDateTime(LocalDate.now(), (String) itemData.get("arrivalTime")))
                    .price(fare)
                    .isSelected(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            planTransportMapper.insert(info);
        }
    }

    private String getAreaCodeFromKeyword(String keyword) {
        if (keyword.contains("서울"))
            return "1";
        if (keyword.contains("부산"))
            return "2";
        if (keyword.contains("대구"))
            return "3";
        if (keyword.contains("인천"))
            return "4";
        if (keyword.contains("경기") || keyword.contains("수원") || keyword.contains("성남"))
            return "31";
        if (keyword.contains("강원") || keyword.contains("춘천") || keyword.contains("강릉"))
            return "32";
        if (keyword.contains("충북") || keyword.contains("청주"))
            return "33";
        if (keyword.contains("충남") || keyword.contains("천안") || keyword.contains("아산"))
            return "34";
        if (keyword.contains("경북") || keyword.contains("포항"))
            return "35";
        if (keyword.contains("경남") || keyword.contains("창원"))
            return "36";
        if (keyword.contains("전북") || keyword.contains("전주"))
            return "37";
        if (keyword.contains("전남") || keyword.contains("광주") || keyword.contains("여수"))
            return "38";
        if (keyword.contains("제주"))
            return "39";
        return null;
    }

    private boolean isRegionKeyword(String keyword) {
        String[] regions = { "서울", "부산", "대구", "인천", "경기", "강원", "충북", "충남",
                "경북", "경남", "전북", "전남", "제주", "수원", "성남", "춘천",
                "강릉", "청주", "천안", "아산", "포항", "창원", "전주", "광주", "여수" };
        for (String region : regions) {
            if (keyword.contains(region)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void addAiItemDirectly(Long planId, Map<String, Object> itemData) {
        String type = (String) itemData.get("type");
        if (type == null) {
            type = (String) itemData.get("recommendationType");
        }
        if (type == null) {
            type = "destination";
        }

        String title = (String) itemData.get("title");
        String address = (String) itemData.getOrDefault("address", "주소 정보 없음 (AI 추천)");
        String keyword = (String) itemData.getOrDefault("image_keyword", "travel");
        String description = (String) itemData.get("description");

        logger.info("➕ [Direct-Add] Request: Title={}, mapx={}, mapX={}, mapy={}, mapY={}",
                title, itemData.get("mapx"), itemData.get("mapX"), itemData.get("mapy"), itemData.get("mapY"));

        String targetDateStr = (String) itemData.get("targetDate");
        LocalDate targetDate = LocalDate.now();
        try {
            if (targetDateStr != null && !targetDateStr.isEmpty()) {
                targetDate = LocalDate.parse(targetDateStr);
            }
        } catch (Exception e) {
            logger.warn("날짜 파싱 실패, 오늘 날짜로 대체합니다: {}", targetDateStr);
        }

        String customContentId = "AI_" + UUID.randomUUID().toString().substring(0, 8);

        String imageUrl = "https://placehold.co/600x400?text=" + keyword;

        String mapX = null;
        String mapY = null;
        if (itemData.get("mapx") != null)
            mapX = String.valueOf(itemData.get("mapx"));
        else if (itemData.get("mapX") != null)
            mapX = String.valueOf(itemData.get("mapX"));

        if (itemData.get("mapy") != null)
            mapY = String.valueOf(itemData.get("mapy"));
        else if (itemData.get("mapY") != null)
            mapY = String.valueOf(itemData.get("mapY"));

        CacheTourApi customCache = CacheTourApi.builder()
                .contentId(customContentId)
                .contentTypeId("accommodation".equals(type) || "hotel".equals(type) ? "32" : "12")
                .title(title)
                .addr1(address)
                .firstImage(imageUrl)
                .overview(description)
                .areaCode("0")
                .sigunguCode("0")
                .cat1("AI")
                .mapX(mapX)
                .mapY(mapY)
                .isPetFriendly(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tourApiCacheMapper.insert(customCache);

        if ("accommodation".equals(type) || "hotel".equals(type)) {
            List<PlanAccommodation> existing = planAccommodationMapper.findByPlanId(planId);
            int nextOrder = existing.stream()
                    .mapToInt(PlanAccommodation::getAccommodationOrder)
                    .max()
                    .orElse(0) + 1;

            PlanAccommodation acc = PlanAccommodation.builder()
                    .planId(planId)
                    .contentId(customContentId)
                    .checkInDate(targetDate)
                    .checkOutDate(targetDate.plusDays(1))
                    .accommodationOrder(nextOrder)
                    .isSelected(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            planAccommodationMapper.insert(acc);
        } else {
            List<PlanSpot> existing = planSpotMapper.findByPlanId(planId);
            int nextOrder = existing.stream()
                    .mapToInt(PlanSpot::getSpotOrder)
                    .max()
                    .orElse(0) + 1;

            PlanSpot dest = PlanSpot.builder()
                    .planId(planId)
                    .contentId(customContentId)
                    .visitDate(targetDate)
                    .spotOrder(nextOrder)
                    .isSelected(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            planSpotMapper.insert(dest);
        }

        Object recIdObj = itemData.get("recommendationId");
        if (recIdObj != null) {
            try {
                Long recId = Long.valueOf(String.valueOf(recIdObj));
                aiRecommendationMapper.delete(recId);
            } catch (Exception e) {
                logger.warn("AI 추천 삭제 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * AI 추천을 계획에 추가
     */
    @Transactional
    public void addAiRecommendationToPlan(Long planId, Map<String, Object> recommendation) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        String type = (String) recommendation.get("type");

        if ("destination".equals(type)) {
            // AI 추천을 관광지로 추가
            AiRecommendation aiRec = AiRecommendation.builder()
                    .planId(planId)
                    .recommendationType(type)
                    .title((String) recommendation.get("title"))
                    .description((String) recommendation.get("description"))
                    .reason((String) recommendation.get("reason"))
                    .createdAt(LocalDateTime.now())
                    .build();
            aiRecommendationMapper.insert(aiRec);
        } else if ("accommodation".equals(type)) {
            // AI 추천을 숙소로 추가
            AiRecommendation aiRec = AiRecommendation.builder()
                    .planId(planId)
                    .recommendationType(type)
                    .title((String) recommendation.get("title"))
                    .description((String) recommendation.get("description"))
                    .reason((String) recommendation.get("reason"))
                    .createdAt(LocalDateTime.now())
                    .build();
            aiRecommendationMapper.insert(aiRec);
        } else if ("transportation".equals(type)) {
            // AI 추천을 교통으로 추가
            AiRecommendation aiRec = AiRecommendation.builder()
                    .planId(planId)
                    .recommendationType(type)
                    .title((String) recommendation.get("title"))
                    .description((String) recommendation.get("description"))
                    .reason((String) recommendation.get("reason"))
                    .createdAt(LocalDateTime.now())
                    .build();
            aiRecommendationMapper.insert(aiRec);
        }
    }

    /**
     * 데이터셋 항목을 계획에 추가
     */
    @Transactional
    public void addDatasetItemToPlan(Long planId, String contentId, String category) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        CacheTourApi cache = tourApiCacheMapper.findByContentId(contentId);
        if (cache == null) {
            throw new RuntimeException("항목을 찾을 수 없습니다");
        }

        if ("destination".equals(category) && "12".equals(cache.getContentTypeId())) {
            PlanSpot planDest = PlanSpot.builder()
                    .planId(planId)
                    .contentId(contentId)
                    .visitDate(plan.getDepartureDate())
                    .isSelected(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            planSpotMapper.insert(planDest);
        } else if ("accommodation".equals(category) && "32".equals(cache.getContentTypeId())) {
            PlanAccommodation planAcc = PlanAccommodation.builder()
                    .planId(planId)
                    .contentId(contentId)
                    .checkInDate(plan.getDepartureDate())
                    .checkOutDate(plan.getArrivalDate())
                    .isSelected(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            planAccommodationMapper.insert(planAcc);
        }
    }

    /**
     * 계획이 없을 때 AI 추천으로 채우기
     */
    @Transactional
    public void fillPlanWithAiRecommendations(Long planId) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("여행 계획을 찾을 수 없습니다");
        }

        String regionName = getRegionName(plan.getArrivalRegionCode());
        String depDate = plan.getDepartureDate() != null ? plan.getDepartureDate().format(DateTimeFormatter.ISO_DATE)
                : "";
        String arrDate = plan.getArrivalDate() != null ? plan.getArrivalDate().format(DateTimeFormatter.ISO_DATE) : "";

        List<Map<String, Object>> aiRecommendations = openAiService.generateTravelRecommendations(
                regionName, depDate, arrDate,
                plan.getAdultCount(), plan.getChildCount(), plan.getHasPet());

        for (Map<String, Object> aiRec : aiRecommendations) {
            AiRecommendation recommendation = AiRecommendation.builder()
                    .planId(planId)
                    .recommendationType((String) aiRec.get("type"))
                    .title((String) aiRec.get("title"))
                    .description((String) aiRec.get("description"))
                    .reason((String) aiRec.get("reason"))
                    .address((String) aiRec.getOrDefault("address", "주소 없음"))
                    .mapX(aiRec.get("mapx") != null ? (String) aiRec.get("mapx") : (String) aiRec.get("mapX"))
                    .mapY(aiRec.get("mapy") != null ? (String) aiRec.get("mapy") : (String) aiRec.get("mapY"))
                    .createdAt(LocalDateTime.now())
                    .build();

            aiRecommendationMapper.insert(recommendation);
        }
    }

    /**
     * 여행 계획 전체 업데이트
     */
    @Transactional
    public void fillPlan(Long planId, TravelPlanRequest request) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null)
            throw new RuntimeException("계획 없음");

        if (request != null) {
            boolean changed = false;

            if (request.getArrivalRegionCode() != null) {
                plan.setArrivalRegionCode(request.getArrivalRegionCode());
                changed = true;
            }
            if (request.getDepartureRegionCode() != null) {
                plan.setDepartureRegionCode(request.getDepartureRegionCode());
                changed = true;
            }
            if (request.getDepartureDate() != null) {
                plan.setDepartureDate(request.getDepartureDate());
                changed = true;
            }
            if (request.getArrivalDate() != null) {
                plan.setArrivalDate(request.getArrivalDate());
                changed = true;
            }
            if (request.getAdultCount() != null) {
                plan.setAdultCount(request.getAdultCount());
                changed = true;
            }
            if (request.getChildCount() != null) {
                plan.setChildCount(request.getChildCount());
                changed = true;
            }
            if (request.getHasPet() != null) {
                plan.setHasPet(request.getHasPet());
                changed = true;
            }

            if (changed) {
                travelPlanMapper.update(plan);
            }
        }

        if (plan.getArrivalRegionCode() == null) {
            throw new RuntimeException("도착지 정보가 없습니다.");
        }

        planSpotMapper.deleteByPlanId(planId);
        planAccommodationMapper.deleteByPlanId(planId);
        planTransportMapper.deleteByPlanId(planId);
        aiRecommendationMapper.deleteByPlanId(planId);

        generateRecommendations(plan);
    }

    /**
     * 아이템 이동 및 날짜 업데이트
     */
    @Transactional
    public void moveItem(Long planId, String type, Map<String, Object> idData, String newDateStr) {
        LocalDate newDate = LocalDate.parse(newDateStr);

        if ("attraction".equals(type) || "destination".equals(type)) {
            int spotOrder = Integer.parseInt(String.valueOf(idData.get("spotOrder")));
            planSpotMapper.updateOrderAndDate(planId, spotOrder, spotOrder, newDate);

        } else if ("hotel".equals(type) || "accommodation".equals(type)) {
            String oldDateStr = (String) idData.get("checkInDate");
            if (oldDateStr.contains("T"))
                oldDateStr = oldDateStr.split("T")[0];

            LocalDate oldDate = LocalDate.parse(oldDateStr);
            int accOrder = Integer.parseInt(String.valueOf(idData.get("accommodationOrder")));

            List<PlanAccommodation> accs = planAccommodationMapper.findByPlanId(planId);
            int maxOrder = 0;
            for (PlanAccommodation acc : accs) {
                if (acc.getCheckInDate().equals(newDate)) {
                    if (acc.getAccommodationOrder() > maxOrder)
                        maxOrder = acc.getAccommodationOrder();
                }
            }
            int newOrder = maxOrder + 1;

            planAccommodationMapper.updateOrderAndDate(planId, oldDate, accOrder, newDate, newOrder);
        }
    }

    /**
     * 계획 제목 수정
     */
    @Transactional
    public void updateTitle(Long planId, String newTitle) {
        TravelPlan plan = travelPlanMapper.findById(planId);
        if (plan == null) {
            throw new RuntimeException("계획을 찾을 수 없습니다.");
        }

        plan.setTitle(newTitle);
        plan.setUpdatedAt(LocalDateTime.now());

        travelPlanMapper.update(plan);
    }

    /**
     * 기차 검색
     */
    public List<Map<String, Object>> searchTrains(String depStation, String arrStation, String dateStr, String time,
            String trainType) {
        LocalDate date = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);

        logger.info("🔍 기차 검색 시작: {} -> {} (날짜: {})", depStation, arrStation, date);
        if (time != null && !time.isEmpty())
            logger.info("🕒 시간 필터 적용: {} 이후 출발", time);

        List<Map<String, Object>> allTrains = korailApiService.searchTrains(depStation, arrStation, date);
        logger.info("📦 API 응답 개수: {}건", allTrains.size());

        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> train : allTrains) {
            String tType = String.valueOf(train.get("trainType"));
            String tTime = String.valueOf(train.get("departureTime"));

            if (trainType != null && !trainType.isEmpty() && !tType.contains(trainType)) {
                continue;
            }

            if (time != null && !time.isEmpty()) {
                if (tTime == null || tTime.length() < 5) {
                    logger.warn("⚠️ 시간 정보가 올바르지 않은 기차 제외: {}", train);
                    continue;
                }

                if (tTime.compareTo(time) < 0) {
                    continue;
                }
            }

            train.put("type", "train");
            filtered.add(train);
        }

        logger.info("✅ 필터링 후 결과: {}건", filtered.size());
        return filtered;
    }

    /**
     * 준비된 관광지/숙소 목록을 여행 일수에 맞춰 날짜를 할당하여 저장합니다.
     */
    private void generatePlanSchedule(TravelPlan plan,
            List<CacheTourApi> allDestinations,
            List<CacheTourApi> allAccommodations,
            List<Map<String, Object>> allTrains) {

        LocalDate startDate = plan.getDepartureDate();
        LocalDate arrivalDate = plan.getArrivalDate();

        if (startDate == null || arrivalDate == null) {
            logger.error("날짜 정보가 없어 일정 분배를 시작할 수 없습니다.");
            return;
        }

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, arrivalDate) + 1;

        int totalAttractions = allDestinations.size();
        int baseCountPerDay = totalAttractions / totalDays;
        int remainder = totalAttractions % totalDays;
        int currentAttractionIndex = 0;
        int nextSpotOrder = 1;

        planSpotMapper.deleteByPlanId(plan.getPlanId());
        planAccommodationMapper.deleteByPlanId(plan.getPlanId());
        planTransportMapper.deleteByPlanId(plan.getPlanId());

        for (int day = 0; day < totalDays; day++) {
            LocalDate targetDate = startDate.plusDays(day);
            int countForThisDay = Math.max(2, baseCountPerDay + (day < remainder ? 1 : 0));
            countForThisDay = Math.min(5, countForThisDay);

            int endIndex = Math.min(currentAttractionIndex + countForThisDay, totalAttractions);
            List<CacheTourApi> dailyDestinations = allDestinations.subList(currentAttractionIndex, endIndex);

            for (CacheTourApi dest : dailyDestinations) {
                PlanSpot planDest = PlanSpot.builder()
                        .planId(plan.getPlanId())
                        .spotOrder(nextSpotOrder++)
                        .contentId(dest.getContentId())
                        .visitDate(targetDate)
                        .isSelected(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                planSpotMapper.insert(planDest);
            }

            currentAttractionIndex = endIndex;
            if (currentAttractionIndex >= totalAttractions)
                break;
        }

        if (!allAccommodations.isEmpty()) {
            CacheTourApi acc = allAccommodations.get(0);
            PlanAccommodation planAcc = PlanAccommodation.builder()
                    .planId(plan.getPlanId())
                    .accommodationOrder(1)
                    .contentId(acc.getContentId())
                    .checkInDate(startDate)
                    .checkOutDate(arrivalDate)
                    .isSelected(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            planAccommodationMapper.insert(planAcc);
        }

        int nextTransportOrder = planTransportMapper.findMaxTransportOrderIndex(plan.getPlanId());

        String depStationNameFromCode = korailApiService.getStationNameByRegionCode(plan.getDepartureRegionCode());
        String arrStationNameFromCode = korailApiService.getStationNameByRegionCode(plan.getArrivalRegionCode());

        try {
            List<Map<String, Object>> outboundTrains = allTrains.stream()
                    .filter(train -> String.valueOf(train.get("departureStation")).equals(depStationNameFromCode) &&
                            String.valueOf(train.get("arrivalStation")).equals(arrStationNameFromCode))
                    .sorted(Comparator.comparing(t -> String.valueOf(t.get("departureTime"))))
                    .collect(java.util.stream.Collectors.toList());

            if (!outboundTrains.isEmpty()) {
                Map<String, Object> trainData = outboundTrains.get(0);

                PlanTransport info = mapTrainDataToEntity(plan.getPlanId(), startDate, trainData);
                info.setTransportOrder(++nextTransportOrder);

                logger.info("--- [DEBUG] TRANSPORT INSERT DATA ---");
                logger.info("Plan ID: {}, Order: {}, Dep Code: {}, Arr Code: {}, Type: {}",
                        info.getPlanId(), info.getTransportOrder(), info.getDepartureStationCode(),
                        info.getArrivalStationCode(), info.getTransportType());
                logger.info("-------------------------------------");
                planTransportMapper.insert(info);
                logger.info("✅ 출발 교통편 저장 완료: {} ({}편)", info.getTransportType(), info.getDepartureTime());
            }
        } catch (Exception e) {
            logger.error("출발 교통편 처리 중 오류", e);
        }

        try {
            String finalDepStation = korailApiService.getStationNameByRegionCode(plan.getArrivalRegionCode());
            String finalArrStation = korailApiService.getStationNameByRegionCode(plan.getDepartureRegionCode());

            List<Map<String, Object>> returnTrainsFiltered = allTrains.stream()
                    .filter(train -> String.valueOf(train.get("departureStation")).equals(finalDepStation) &&
                            String.valueOf(train.get("arrivalStation")).equals(finalArrStation))
                    .filter(train -> String.valueOf(train.get("departureTime")).compareTo("14:00") >= 0)
                    .sorted(Comparator.comparing(t -> String.valueOf(t.get("departureTime"))))
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> trainData;

            if (!returnTrainsFiltered.isEmpty()) {
                trainData = returnTrainsFiltered.get(0);
            } else if (!allTrains.isEmpty()) {
                trainData = allTrains.stream()
                        .filter(train -> String.valueOf(train.get("departureStation")).equals(finalDepStation) &&
                                String.valueOf(train.get("arrivalStation")).equals(finalArrStation))
                        .max(Comparator.comparing(t -> String.valueOf(t.get("departureTime"))))
                        .orElse(null);
            } else {
                return;
            }

            if (trainData != null) {
                PlanTransport info = mapTrainDataToEntity(plan.getPlanId(), arrivalDate, trainData);
                info.setTransportOrder(++nextTransportOrder);

                logger.info("--- [DEBUG] RETURN TRANSPORT INSERT DATA ---");
                logger.info("Plan ID: {}, Order: {}, Dep Code: {}, Arr Code: {}, Type: {}",
                        info.getPlanId(), info.getTransportOrder(), info.getDepartureStationCode(),
                        info.getArrivalStationCode(), info.getTransportType());
                logger.info("-------------------------------------");

                planTransportMapper.insert(info);
                logger.info("✅ 복귀 교통편 저장 완료.");
            }
        } catch (Exception e) {
            logger.error("복귀 교통편 처리 중 오류", e);
        }
    }

    /**
     * Korail API 결과를 TransportationInfo 엔티티로 매핑
     */
    private PlanTransport mapTrainDataToEntity(Long planId, LocalDate travelDate, Map<String, Object> train) {
        try {
            String departureStationName = (String) train.getOrDefault("depplacename", train.get("departureStation"));
            String arrivalStationName = (String) train.getOrDefault("arrplacename", train.get("arrivalStation"));

            if (departureStationName == null || arrivalStationName == null) {
                logger.error("⚠️ 역 이름 정보 누락: {}", train);
                return null;
            }

            String depCode = korailApiService.getStationCode(departureStationName);
            String arrCode = korailApiService.getStationCode(arrivalStationName);

            if (depCode == null || arrCode == null) {
                // 역 코드 매핑 실패 시 로그 출력
                logger.warn("역 코드 매핑 실패: {} -> {}, {} -> {}",
                        departureStationName, depCode, arrivalStationName, arrCode);
                return null;
            }

            String trainType = (String) train.getOrDefault("traintype", train.getOrDefault("trainType", "기차"));

            Object depTimeObj = train.getOrDefault("depplandtime", train.get("departureTime"));
            Object arrTimeObj = train.getOrDefault("arrplandtime", train.get("arrivalTime"));

            LocalDateTime depTime = null;
            LocalDateTime arrTime = null;

            if (depTimeObj != null && String.valueOf(depTimeObj).length() == 14) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                depTime = LocalDateTime.parse(String.valueOf(depTimeObj), formatter);
            } else if (depTimeObj != null) {
                depTime = parseTimeToDateTime(travelDate, String.valueOf(depTimeObj));
            }

            if (arrTimeObj != null && String.valueOf(arrTimeObj).length() == 14) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                arrTime = LocalDateTime.parse(String.valueOf(arrTimeObj), formatter);
            } else if (arrTimeObj != null) {
                arrTime = parseTimeToDateTime(travelDate, String.valueOf(arrTimeObj));
            }

            int fare = 0;
            Object fareObj = train.getOrDefault("adultcharge", train.get("fare"));
            if (fareObj != null) {
                try {
                    fare = Integer.parseInt(String.valueOf(fareObj).replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    fare = 0;
                }
            }

            return PlanTransport.builder()
                    .planId(planId)
                    .transportType(trainType)
                    .departureStationCode(depCode)
                    .arrivalStationCode(arrCode)
                    .departureTime(depTime != null ? depTime : travelDate.atStartOfDay())
                    .arrivalTime(arrTime != null ? arrTime : travelDate.atStartOfDay())
                    .price(fare)
                    .reservationUrl("https://www.letskorail.com")
                    .isSelected(true)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            logger.error("열차 데이터 매핑 중 오류 발생", e);
            return null;
        }
    }
}
