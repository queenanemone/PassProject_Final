package com.tripboard.service;

import com.tripboard.entity.RecordImage;
import com.tripboard.entity.TravelRecord;
import com.tripboard.mapper.RecordImageMapper;
import com.tripboard.mapper.TravelRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelRecordService {

    private final TravelRecordMapper travelRecordMapper;
    private final RecordImageMapper recordImageMapper;

    /**
     * 여행 기록 생성
     */
    @Transactional
    public TravelRecord createRecord(Long userId, Long planId, String title, String content) {
        TravelRecord record = TravelRecord.builder()
                .userId(userId)
                .planId(planId != null && planId > 0 ? planId : null)
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        travelRecordMapper.insert(record);
        return record;
    }

    /**
     * 여행 기록 이미지 추가
     */
    @Transactional
    public void addImage(Long recordId, String imageUrl, Integer order) {
        RecordImage image = RecordImage.builder()
                .recordId(recordId)
                .imageUrl(imageUrl)
                .imageOrder(order)
                .createdAt(LocalDateTime.now())
                .build();

        recordImageMapper.insert(image);
    }

    /**
     * 계획 ID로 여행 기록 조회
     */
    public List<TravelRecord> getRecordsByPlanId(Long planId) {
        return travelRecordMapper.findByPlanId(planId);
    }

    /**
     * 사용자별 여행 기록 조회
     */
    public List<TravelRecord> getUserRecords(Long userId) {
        return travelRecordMapper.findByUserId(userId);
    }

    /**
     * 여행 기록 상세 조회
     */
    public TravelRecord getRecord(Long recordId) {
        return travelRecordMapper.findById(recordId);
    }

    /**
     * 여행 기록 이미지 목록 조회
     */
    public List<RecordImage> getRecordImages(Long recordId) {
        return recordImageMapper.findByRecordId(recordId);
    }

    /**
     * 여행 기록 수정
     */
    @Transactional
    public void updateRecord(Long recordId, String title, String content) {
        TravelRecord record = travelRecordMapper.findById(recordId);
        if (record != null) {
            record.setTitle(title);
            record.setContent(content);
            record.setUpdatedAt(LocalDateTime.now());
            travelRecordMapper.update(record);
        }
    }

    /**
     * 여행 기록 삭제
     */
    @Transactional
    public void deleteRecord(Long recordId) {
        travelRecordMapper.delete(recordId);
    }
}
