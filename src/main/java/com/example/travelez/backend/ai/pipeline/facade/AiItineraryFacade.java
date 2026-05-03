package com.example.travelez.backend.ai.pipeline.facade;

import com.example.travelez.backend.ai.pipeline.model.EvaluationReport;
import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase1StrategyCompiler;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase2VectorRetrieval;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase3Generation;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase4Evaluation;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase5Correction;
import com.example.travelez.backend.ai.pipeline.subsystem.Phase6Enrichment;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItineraryReplanRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.UserProfileVector;
import com.example.travelez.backend.users.repository.UserProfileVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiItineraryFacade {

    private final Phase1StrategyCompiler compiler;
    private final Phase2VectorRetrieval retriever;
    private final Phase3Generation generator;
    private final Phase4Evaluation evaluator;
    private final Phase5Correction corrector;
    private final Phase6Enrichment enricher;
    private final UserProfileVectorRepository userProfileVectorRepository;

    private String getCurrentUserProfileVector() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        // Ép kiểu để lấy ID
        UserPrinciple principle = (UserPrinciple) authentication.getPrincipal();
        Long userId = principle.getUserId();

        return userProfileVectorRepository.findById(userId)
                .map(UserProfileVector::getProfileVector)
                .filter(vector -> !vector.isBlank())
                .orElse(null);
    }

    public ItineraryResponse orchestratePipeline(ItineraryCreationRequest request) {
        log.info("================ STARTING AI ITINERARY PIPELINE ================");

        // 1. Lấy thông tin Vector Sở thích của User đang gọi API
        String userProfileVector = getCurrentUserProfileVector();

        PipelineContext context = PipelineContext.builder()
                .originalRequest(request)
                .userProfileVector(userProfileVector)
                .build();

        // PHASE 1: Tạo Search Queries
        context.setSearchQueries(compiler.generateSearchQueries(request));

        // PHASE 2: Tìm POI tương đồng bằng pgvector
        context.setRetrievedPois(retriever.retrieveMatchingPois(context));

        // PHASE 3: Sinh Lịch Trình (Raw JSON từ LLM)
        context.setRawLlmResponse(generator.generateItinerary(context));

        // PHASE 4: Kiểm duyệt bằng Logic OSRM, Giờ mở cửa, Budget
        EvaluationReport report = evaluator.evaluate(context);

        // PHASE 5: Tự Sửa Lỗi nếu Phase 4 gặp lỗi
        if (!report.isPassed()) {
            log.warn("Phase 4 caught errors ({}). Initiating Phase 5 (LLM2 Correction)...", report.getErrors().size());
            String fixedJson = corrector.fixItineraryWithLlm2(context, report);
            context.setRawLlmResponse(fixedJson); // Nạp lại JSON chuẩn
        } else {
            log.info("Phase 4 PASSED on first try! Skipping Phase 5.");
        }

        // PHASE 6: Parse sang đối tượng & đắp thêm dữ liệu từ DB (Ảnh, Toạ độ, Địa chỉ)
        ItineraryResponse finalResponse = enricher.parseAndEnrich(context);

        // Finalize: Đảm bảo gán lại Context đầu vào để tránh LLM chế lung tung
        if (finalResponse != null) {
            finalResponse.setDestinationCities(request.getDestinationCities());
            finalResponse.setTempId(UUID.randomUUID().toString());
        }

        context.setFinalResponse(finalResponse);

        log.info("================ FINISHED AI ITINERARY PIPELINE ================");
        return finalResponse;
    }

    public ItineraryResponse orchestrateReplanPipeline(ItineraryReplanRequest request) {
        log.info("================ STARTING AI REPLAN PIPELINE ================");

        String userProfileVector = getCurrentUserProfileVector();

        PipelineContext context = PipelineContext.builder()
                .originalRequest(request)
                .userProfileVector(userProfileVector)
                .build();

        // PHASE 1: Tạo Search Queries
        context.setSearchQueries(compiler.generateSearchQueries(request));

        // PHASE 2: Tìm POI tương đồng & Lọc bỏ các POI user đã reject
        context.setRetrievedPois(retriever.retrieveForReplan(request, context));

        // PHASE 3: Replan lịch trình
        String replanJson = generator.generateReplanItinerary(context, request);
        context.setRawLlmResponse(replanJson);

        // PHASE 4: Kiểm duyệt Logistics, Giờ mở cửa, Budget
        EvaluationReport report = evaluator.evaluate(context);

        // PHASE 5: Tự Sửa Lỗi nếu Phase 4 bắt được lỗi
        if (!report.isPassed()) {
            log.warn("Replan Phase 4 caught errors ({}). Initiating Phase 5 (LLM2 Correction)...", report.getErrors().size());
            String fixedJson = corrector.fixItineraryWithLlm2(context, report);
            context.setRawLlmResponse(fixedJson);
        } else {
            log.info("Replan Phase 4 PASSED on first try! Skipping Phase 5.");
        }

        // PHASE 6: Parse sang Entity và đắp thêm data DB
        ItineraryResponse finalResponse = enricher.parseAndEnrich(context);

        if (finalResponse != null) {
            finalResponse.setDestinationCities(request.getDestinationCities());
            finalResponse.setTempId(java.util.UUID.randomUUID().toString());
        }

        context.setFinalResponse(finalResponse);

        log.info("================ FINISHED AI REPLAN PIPELINE ================");
        return finalResponse;
    }

}