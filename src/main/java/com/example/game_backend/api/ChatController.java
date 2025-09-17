// ChatController.java
package com.example.game_backend.api;

import com.example.game_backend.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final McpService mcpService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("question"); // 기존 코드에 맞춤

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("질문이 비어있습니다.");
        }

        log.info("사용자 질문 수신: {}", userMessage);

        String response = mcpService.askMcp(userMessage);

        log.info("챗봇 응답 전송 완료");
        return ResponseEntity.ok(response);
    }
}