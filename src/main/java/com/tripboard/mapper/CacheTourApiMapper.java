package com.tripboard.mapper;

import com.tripboard.entity.CacheTourApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CacheTourApiMapper {
        CacheTourApi findByContentId(String contentId);

        List<CacheTourApi> findByAreaCode(@Param("areaCode") String areaCode,
                        @Param("contentTypeId") String contentTypeId,
                        @Param("hasPet") Boolean hasPet,
                        @Param("limit") Integer limit);

        List<CacheTourApi> searchByKeyword(@Param("keyword") String keyword,
                        @Param("contentTypeId") String contentTypeId,
                        @Param("limit") Integer limit);

        void insert(CacheTourApi cache);

        void update(CacheTourApi cache);
}
