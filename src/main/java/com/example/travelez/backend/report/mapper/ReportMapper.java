package com.example.travelez.backend.report.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface ReportMapper {
}
