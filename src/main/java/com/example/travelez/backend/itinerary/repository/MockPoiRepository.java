package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.ErrorCode;
import com.example.travelez.backend.itinerary.model.PoiData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Repository
public class MockPoiRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<PoiData> findPoisByDestination(String destination){
        try {
            // 1. Đọc file JSON từ thư mục resources/mock_data
            ClassPathResource resource = new ClassPathResource("mock_data/dalat_pois.json");

            // Kiểm tra file có tồn tại không để tránh lỗi NullPointerException
            if (!resource.exists()) {
                System.out.println("⚠️ Cảnh báo: Không tìm thấy file mock_data/dalat_pois.json");
                return Collections.emptyList();
            }

            InputStream inputStream = resource.getInputStream();
            List<PoiData> allPois = objectMapper.readValue(inputStream, new TypeReference<List<PoiData>>() {});

            // 2. Filter đơn giản (Demo)
            // Nếu intent là null hoặc rỗng -> trả về rỗng
            if (destination == null) {
                return Collections.emptyList();
            }

            // Nếu người dùng hỏi "Đà Lạt" (hoặc Da Lat) -> trả về list mock
            // Logic này để demo rằng hệ thống có check địa điểm chứ không trả bừa
            String destLower = destination.toLowerCase();
            if (destLower.contains("đà lạt") || destLower.contains("da lat")) {
                return allPois;
            }

            // Nếu hỏi địa điểm khác (VD: Nha Trang) -> Trả về rỗng (để Service báo lỗi chưa hỗ trợ)
            return Collections.emptyList();

        } catch (Exception e) {
            e.printStackTrace();
            // Trong môi trường dev/demo, in lỗi ra console để dễ fix
            throw new ApiException(ErrorCode.FAILED, "Lỗi đọc dữ liệu Mock: " + e.getMessage());
        }
    }
}
