package com.tripboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripboard.entity.CacheRailwayStation;
import com.tripboard.mapper.CacheRailwayStationMapper;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KorailApiService {
	private static final Logger logger = LoggerFactory.getLogger(KorailApiService.class);

	@Value("${korail.api.key}")
	private String apiKey;

	@Value("${korail.api.base-url}")
	private String baseUrl;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final CacheRailwayStationMapper cacheRailwayStationMapper;

	private static final Map<String, String> STATION_CODES = new HashMap<>();

	static {
		// 서울/수도권
		STATION_CODES.put("서울", "NAT010000");
		STATION_CODES.put("용산", "NAT010032");
		STATION_CODES.put("영등포", "NAT010415");
		STATION_CODES.put("광명", "NAT010415");
		STATION_CODES.put("수원", "NAT010754");
		STATION_CODES.put("천안아산", "NAT011668");

		// 충청권
		STATION_CODES.put("대전", "NAT011668");
		STATION_CODES.put("서대전", "NAT011700");
		STATION_CODES.put("청주", "NAT012251");

		// 호남권
		STATION_CODES.put("광주송정", "NAT031345");
		STATION_CODES.put("전주", "NAT013843");
		STATION_CODES.put("익산", "NAT013271");
		STATION_CODES.put("목포", "NAT031091");
		STATION_CODES.put("여수엑스포", "NAT032052");

		// 경상권
		STATION_CODES.put("동대구", "NAT013271");
		STATION_CODES.put("김천구미", "NAT012548");
		STATION_CODES.put("경주", "NAT013271");
		STATION_CODES.put("포항", "NAT013271");
		STATION_CODES.put("울산", "NAT013271");
		STATION_CODES.put("부산", "NAT014445");
		STATION_CODES.put("마산", "NAT014455");
		STATION_CODES.put("진주", "NAT014487");

		// 강원권
		STATION_CODES.put("강릉", "NAT800490");
		STATION_CODES.put("동해", "NAT800491");
		STATION_CODES.put("평창", "NAT800488");
		STATION_CODES.put("정동진", "NAT800492");
	}

	/**
	 * 지역 코드로 대표 역 이름 가져오기
	 */
	public String getStationNameByRegionCode(String regionCode) {
		Map<String, String> regionMap = new HashMap<>();

		regionMap.put("1", "서울");
		regionMap.put("2", "인천");
		regionMap.put("3", "대전");
		regionMap.put("4", "동대구");
		regionMap.put("5", "광주송정");
		regionMap.put("6", "부산");
		regionMap.put("7", "울산(통도사)");
		regionMap.put("8", "오송");

		regionMap.put("31", "수원");
		regionMap.put("32", "강릉");
		regionMap.put("33", "오송");
		regionMap.put("34", "천안아산");
		regionMap.put("35", "신경주");
		regionMap.put("36", "마산");
		regionMap.put("37", "전주");
		regionMap.put("38", "여수엑스포");

		return regionMap.getOrDefault(regionCode, "서울");
	}

	/**
	 * 역 이름으로 역 코드를 가져옵니다.
	 * DB 캐시를 먼저 확인하고, 없으면 API를 호출하여 캐시를 채우는 로직을 수행합니다.
	 */
	public String getStationCode(String stationName) {
		if (stationName == null || stationName.isEmpty())
			return null;

		String stationCode = cacheRailwayStationMapper.findStationCodeByName(stationName);

		if (stationCode == null) {
			logger.warn("⚠️ 역 캐시 미스: '{}' 역 코드를 찾을 수 없습니다. API를 호출하여 캐시 채우기 시도.", stationName);
			stationCode = attemptCacheAndRetrieve(stationName);
		}

		if (stationCode == null) {
			logger.warn("역 코드를 최종적으로 찾을 수 없습니다: {}. null 반환", stationName);
		}

		return stationCode;
	}

	private String attemptCacheAndRetrieve(String stationName) {
		try {
			logger.warn("⚠️ 역 캐시 미스: '{}' 역 코드를 찾을 수 없습니다. API 전체 캐싱을 시도합니다.", stationName);

			List<CacheRailwayStation> allStations = fetchAllStationsFromApi();

			if (!allStations.isEmpty()) {
				cacheRailwayStationMapper.insertAll(allStations);
				return cacheRailwayStationMapper.findStationCodeByName(stationName);
			}
		} catch (Exception e) {
			logger.error("역 캐싱 중 오류 발생: {}", e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 열차 시간표 조회
	 * 
	 * @param depPlaceId     출발역 코드
	 * @param arrPlaceId     도착역 코드
	 * @param depPlandTime   출발 날짜 (yyyyMMdd)
	 * @param trainGradeCode 열차 종류 (00: 전체, 01: KTX, 02: 새마을, 03: 무궁화 등)
	 * @return 열차 시간표 목록
	 */
	public List<Map<String, Object>> getTrainSchedule(
			String depPlaceId,
			String arrPlaceId,
			String depPlandTime,
			String trainGradeCode) {
		try {
			URI uri = UriComponentsBuilder
					.fromHttpUrl(baseUrl + "/getStrtpntAlocFndTrainInfo")
					.queryParam("serviceKey", apiKey)
					.queryParam("depPlaceId", depPlaceId)
					.queryParam("arrPlaceId", arrPlaceId)
					.queryParam("depPlandTime", depPlandTime)
					.queryParam("trainGradeCode", trainGradeCode)
					.queryParam("numOfRows", "100")
					.queryParam("pageNo", "1")
					.queryParam("_type", "json")
					.build(true)
					.toUri();

			logger.info("코레일 API 호출: {}", uri);

			String response = restTemplate.getForObject(uri, String.class);
			logger.debug("코레일 API 응답: {}", response);

			return parseTrainScheduleResponse(response);
		} catch (Exception e) {
			logger.error("코레일 API 호출 실패: {}", e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * 열차 시간표 조회 (편의 메서드)
	 */
	public List<Map<String, Object>> searchTrains(
			String departureStation,
			String arrivalStation,
			LocalDate date) {
		String depCode = getStationCode(departureStation);
		String arrCode = getStationCode(arrivalStation);
		String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		List<Map<String, Object>> ktxResults = getTrainSchedule(depCode, arrCode, dateStr, "01");

		if (ktxResults.isEmpty()) {
			logger.info("KTX 조회 결과 없음. 전체 열차 조회 시도");
			return getTrainSchedule(depCode, arrCode, dateStr, "00");
		}

		return ktxResults;
	}

	/**
	 * API 응답 파싱
	 */
	private List<Map<String, Object>> parseTrainScheduleResponse(String response) {
		logger.info("--- [DEBUG] KORAIL API RAW RESPONSE START ---");
		logger.info("{}", response);
		logger.info("--- [DEBUG] KORAIL API RAW RESPONSE END ---");
		List<Map<String, Object>> results = new ArrayList<>();

		try {
			JsonNode root = objectMapper.readTree(response);
			JsonNode items = root.path("response").path("body").path("items").path("item");

			if (root.path("response").path("body").path("totalCount").asInt() == 0 || items.isMissingNode()) {
				return new ArrayList<>();
			}
			if (items.isArray()) {
				for (JsonNode item : items) {
					Map<String, Object> train = new HashMap<>();

					String trainType = item.path("traingradename").asText();
					train.put("trainType", trainType);
					train.put("trainNo", item.path("trainno").asText());
					train.put("departureStation", item.path("depplacename").asText());
					train.put("arrivalStation", item.path("arrplacename").asText());
					train.put("departureTime", formatTime(item.path("depplandtime").asText()));
					train.put("arrivalTime", formatTime(item.path("arrplandtime").asText()));

					int fare = 0;
					try {
						fare = item.path("adultcharge").asInt();
					} catch (Exception e) {
						fare = 0;
					}

					if (fare == 0) {
						if (trainType.contains("KTX") || trainType.contains("SRT")) {
							fare = 59800;
						} else if (trainType.contains("ITX") || trainType.contains("새마을")) {
							fare = 42600;
						} else if (trainType.contains("무궁화") || trainType.contains("누리로")) {
							fare = 28600;
						} else {
							fare = 10000;
						}
					}

					train.put("fare", fare);
					results.add(train);
				}
			}
		} catch (Exception e) {
			logger.error("응답 파싱 실패: {}", e.getMessage(), e);
		}

		return results;
	}

	/**
	 * 열차 등급 코드를 이름으로 변환
	 */
	private String getTrainTypeName(String gradeCode) {
		switch (gradeCode) {
			case "01":
			case "02":
			case "03":
			case "04":
			case "05":
			case "06":
			case "07":
			case "08":
			case "09":
				return "KTX";
			case "10":
				return "새마을호";
			case "11":
				return "무궁화호";
			case "12":
				return "통근열차";
			case "13":
				return "ITX-새마을";
			case "14":
				return "ITX-청춘";
			case "15":
				return "SRT";
			default:
				return "일반열차";
		}
	}

	/**
	 * 시간 포맷 변환 (YYYYMMDDHHMMSS → HH:mm)
	 */
	private String formatTime(String time) {
		if (time == null || time.length() < 12) {
			return "";
		}

		try {
			String hour = time.substring(8, 10);
			String minute = time.substring(10, 12);
			return hour + ":" + minute;
		} catch (Exception e) {
			logger.warn("시간 파싱 오류: {}", time);
			return "";
		}
	}

	public List<CacheRailwayStation> fetchAllStationsFromApi() throws Exception {
		List<Map<String, String>> cityCodes = fetchAllCityCodes();
		List<CacheRailwayStation> allStations = new ArrayList<>();

		if (cityCodes.isEmpty()) {
			logger.warn("⚠️ 도시 코드 목록을 가져올 수 없어 역 캐싱을 진행할 수 없습니다.");
			return allStations;
		}

		for (Map<String, String> city : cityCodes) {
			String cityCode = city.get("cityCode");

			URI uri = UriComponentsBuilder
					.fromHttpUrl(baseUrl + "/getCtyAcctoTrainSttnList")
					.queryParam("serviceKey", apiKey)
					.queryParam("cityCode", cityCode)
					.queryParam("numOfRows", "300")
					.queryParam("_type", "json")
					.build(true)
					.toUri();

			String response = restTemplate.getForObject(uri, String.class);

			allStations.addAll(parseStationsFromCityResponse(response));
		}

		logger.info("✅ API 전체 캐싱 완료. 총 {}개의 역 정보 획득.", allStations.size());
		return allStations;
	}

	private List<CacheRailwayStation> parseStationsFromCityResponse(String response) {
		List<CacheRailwayStation> stations = new ArrayList<>();
		try {
			JsonNode root = objectMapper.readTree(response);
			JsonNode items = root.path("response").path("body").path("items").path("item");

			if (items.isArray()) {
				for (JsonNode item : items) {

					stations.add(CacheRailwayStation.builder()
							.stationCode(item.path("nodeid").asText())
							.stationName(item.path("nodename").asText())
							.cityCode(item.path("cityCode").asText())
							.cityName(item.path("cityname").asText())
							.build());
				}
			}
		} catch (Exception e) {
			logger.error("역 목록 API 응답 파싱 실패", e);
		}
		return stations;
	}

	/**
	 * API 4: 도시코드 목록을 조회합니다.
	 */
	private List<Map<String, String>> fetchAllCityCodes() throws Exception {
		List<Map<String, String>> cityCodes = new ArrayList<>();

		URI uri = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/getCtyCodeList")
				.queryParam("serviceKey", apiKey)
				.queryParam("_type", "json")
				.build(true)
				.toUri();

		String response = restTemplate.getForObject(uri, String.class);
		JsonNode root = objectMapper.readTree(response);
		JsonNode items = root.path("response").path("body").path("items").path("item");

		if (items.isArray()) {
			for (JsonNode item : items) {
				Map<String, String> city = new HashMap<>();
				city.put("cityCode", item.path("citycode").asText());
				city.put("cityName", item.path("cityname").asText());
				cityCodes.add(city);
			}
		}
		return cityCodes;
	}

	/**
	 * API 응답을 CacheRailwayStation 엔티티 리스트로 파싱
	 */
	private List<CacheRailwayStation> parseStationResponse(String response) {
		List<CacheRailwayStation> stations = new ArrayList<>();
		try {
			JsonNode root = objectMapper.readTree(response);
			JsonNode items = root.path("response").path("body").path("items").path("item");

			if (items.isArray()) {
				for (JsonNode item : items) {
					stations.add(CacheRailwayStation.builder()
							.stationCode(item.path("stationId").asText())
							.stationName(item.path("stationName").asText())
							.cityCode(item.path("cityCode").asText())
							.cityName(item.path("cityName").asText())
							.lineName(item.path("lineName").asText())
							.regionCode(item.path("regionCode").asText())
							.build());
				}
			}
		} catch (Exception e) {
			logger.error("역 목록 API 응답 파싱 실패", e);
		}
		return stations;
	}
}