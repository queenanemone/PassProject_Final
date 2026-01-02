package com.tripboard.mapper;

import com.tripboard.entity.RecordImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecordImageMapper {
    List<RecordImage> findByRecordId(Long recordId);
    void insert(RecordImage image);
    void delete(Long imageId);
}

