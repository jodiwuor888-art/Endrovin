package com.portfolio.controller;

import com.portfolio.dto.AiAssistRequest;
import com.portfolio.dto.AiAssistResponse;
import com.portfolio.dto.ApiResponse;
import com.portfolio.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin AI Assistant", description = "AI-assisted technical copy polish, SEO optimization, and portfolio audit")
public class AdminAiAssistantController {

    private final AiAssistantService aiService;

    public AdminAiAssistantController(AiAssistantService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/assist")
    @Operation(summary = "Process AI assistance prompt (enhance project, generate SEO, classify inquiry, audit)")
    public ResponseEntity<ApiResponse<AiAssistResponse>> processAssist(@RequestBody AiAssistRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Assistant suggestions generated", aiService.processAiRequest(req)));
    }
}
