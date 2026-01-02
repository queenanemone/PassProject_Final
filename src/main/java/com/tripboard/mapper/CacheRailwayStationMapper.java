package com.tripboard.mapper;

import com.tripboard.entity.CacheRailwayStation; // 엔티티 파일은 있다고 가정
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CacheRailwayStationMapper {

    /**
     * 캐시된 역 정보(PK)를 기반으로 단일 항목을 조회합니다.
     */
    CacheRailwayStation findByStationCode(@Param("stationName") String stationCode);

    /**
     * 새로운 역 정보를 캐시 테이블에 삽입합니다.
     */
    void insert(CacheRailwayStation station);

    /**
     * 💡 [핵심 기능] 역 이름(예: '서울')을 기반으로 역 코드(예: 'NAT010000')를 조회합니다.
     * 이 코드가 PlanTransport 테이블의 FK 제약 조건을 만족시키는 데 사용됩니다.
     */
    String findStationCodeByName(@Param("stationName") String stationName);

    /**
     * 모든 역 목록을 조회합니다. (예: 초기 로딩 시)
     */
    List<CacheRailwayStation> findAll();

    // CacheRailwayStationMapper.java 인터페이스에 추가

    /**
     * API에서 가져온 전체 역 목록을 DB에 한 번에 삽입합니다.
     */
    void insertAll(@Param("stations") List<CacheRailwayStation> stations);
}