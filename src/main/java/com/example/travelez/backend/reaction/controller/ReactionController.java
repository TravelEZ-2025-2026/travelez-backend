package com.example.travelez.backend.reaction.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.reaction.dto.request.ReactionToggleRequest;
import com.example.travelez.backend.reaction.dto.response.ReactorsResponse;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.reaction.service.ReactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "Reactions endpoints")
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping("/toggle")
    public ResponseEntity<BaseResponse<Void>> toggleReaction(@RequestBody @Valid ReactionToggleRequest request) {
        reactionService.toggleReaction(request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Reaction toggled successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<BaseResponse<CommonPage<ReactorsResponse>>> getReactors(
            @RequestParam(name = "targetType") ReactionTargetType targetType,
            @RequestParam(name = "targetId") Long targetId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest request = new PaginationRequest(page, size, "createdAt", Sort.Direction.DESC);
        CommonPage<ReactorsResponse> response = reactionService.getReactors(targetType, targetId, PaginationUtils.getPageable(request));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Reactors fetched successfully");
    }
}
