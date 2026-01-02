package com.tripboard.service;

import com.tripboard.dto.HotPlaceRequest;
import com.tripboard.entity.HotPlace;
import com.tripboard.entity.HotPlaceImage;
import com.tripboard.mapper.HotPlaceImageMapper;
import com.tripboard.mapper.HotPlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotPlaceService {

    private final HotPlaceMapper hotPlaceMapper;
    private final HotPlaceImageMapper hotPlaceImageMapper;

    /**
     * HotPlace 생성
     */
    @Transactional
    public HotPlace createHotPlace(Long userId, HotPlaceRequest request) {
        HotPlace hotPlace = HotPlace.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitudeAsBigDecimal())
                .longitude(request.getLongitudeAsBigDecimal())
                .contentId(request.getContentId())
                .contentTypeId(request.getContentTypeId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotPlaceMapper.insert(hotPlace);
        return hotPlace;
    }

    /**
     * HotPlace 이미지 추가
     */
    @Transactional
    public void addImage(Long hotplaceId, String imageUrl, Integer order) {
        HotPlaceImage image = HotPlaceImage.builder()
                .hotplaceId(hotplaceId)
                .imageUrl(imageUrl)
                .imageOrder(order)
                .createdAt(LocalDateTime.now())
                .build();

        hotPlaceImageMapper.insert(image);
    }

    /**
     * 사용자별 HotPlace 조회
     */
    public List<HotPlace> getUserHotPlaces(Long userId) {
        return hotPlaceMapper.findByUserId(userId);
    }

    /**
     * 모든 HotPlace 조회
     */
    public List<HotPlace> getAllHotPlaces() {
        return hotPlaceMapper.findAll();
    }

    /**
     * HotPlace 상세 조회
     */
    public HotPlace getHotPlace(Long hotplaceId) {
        return hotPlaceMapper.findById(hotplaceId);
    }

    /**
     * HotPlace 이미지 목록 조회
     */
    public List<HotPlaceImage> getHotPlaceImages(Long hotplaceId) {
        return hotPlaceImageMapper.findByHotplaceId(hotplaceId);
    }

    /**
     * HotPlace 수정
     */
    @Transactional
    public void updateHotPlace(Long hotplaceId, HotPlaceRequest request) {
        HotPlace hotPlace = hotPlaceMapper.findById(hotplaceId);
        if (hotPlace != null) {
            hotPlace.setTitle(request.getTitle());
            hotPlace.setDescription(request.getDescription());
            hotPlace.setAddress(request.getAddress());
            hotPlace.setLatitude(request.getLatitudeAsBigDecimal());
            hotPlace.setLongitude(request.getLongitudeAsBigDecimal());
            hotPlace.setContentId(request.getContentId());
            hotPlace.setContentTypeId(request.getContentTypeId());
            hotPlace.setUpdatedAt(LocalDateTime.now());
            hotPlaceMapper.update(hotPlace);
        }
    }

    /**
     * HotPlace 삭제
     */
    @Transactional
    public void deleteHotPlace(Long hotplaceId) {
        hotPlaceMapper.delete(hotplaceId);
    }
}

