package com.example.game_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpServiceImpl implements McpService {

    private final RestTemplate restTemplate;

    @Value("${mcp.server.url:http://localhost:3003}")  // LangChain 서버 포트
    private String mcpServerUrl;

    @Value("${mcp.database.name:mcp_db}")
    private String databaseName;

    @Value("${mcp.vector.store.name:knowledge_base}")
    private String vectorStoreName;

    @Value("${mcp.session.timeout:3600}")  // 세션 타임아웃 (초)
    private int sessionTimeout;

    @Override
    public String askMcp(String question) {
        return askMcp(question, null);
    }

    @Override
    public String askMcp(String question, String sessionId) {
        try {
            log.info("LangChain MCP에 질문 전달: question={}, sessionId={}", question, sessionId);

            // 세션 ID가 없으면 생성
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = generateSessionId();
                log.info("새 세션 ID 생성: {}", sessionId);
            }

            String result = chatWithLangChain(question, sessionId);

            log.info("LangChain MCP 응답 수신 완료: sessionId={}", sessionId);
            return result;

        } catch (RestClientException e) {
            log.error("MCP 서버 연결 오류: {}", e.getMessage());
            return createErrorResponse("챗봇 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("MCP 서비스 호출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return createErrorResponse("서비스 처리 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
        }
    }

    private String chatWithLangChain(String question, String sessionId) {
        String url = mcpServerUrl + "/chat";

        Map<String, Object> request = new HashMap<>();
        request.put("user_query", question);
        request.put("session_id", sessionId);
        request.put("database_name", databaseName);
        request.put("vector_store_name", vectorStoreName);

        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.debug("LangChain 요청: url={}, request={}", url, request);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        log.debug("LangChain 응답: status={}, body={}",
                response.getStatusCode(),
                response.getBody());

        return response.getBody();
    }

    @Override
    public void clearConversation(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("세션 ID가 비어있어 대화 기록을 삭제할 수 없습니다.");
            return;
        }

        try {
            String url = mcpServerUrl + "/chat/" + sessionId;

            log.info("대화 기록 삭제 요청: sessionId={}", sessionId);

            restTemplate.delete(url);

            log.info("대화 기록 삭제 완료: sessionId={}", sessionId);

        } catch (RestClientException e) {
            log.error("대화 기록 삭제 중 네트워크 오류: sessionId={}, error={}", sessionId, e.getMessage());
        } catch (Exception e) {
            log.error("대화 기록 삭제 중 예상치 못한 오류: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public boolean isServiceHealthy() {
        try {
            String url = mcpServerUrl + "/health";

            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            log.debug("MCP 서비스 상태 확인: healthy={}", isHealthy);

            return isHealthy;

        } catch (Exception e) {
            log.warn("MCP 서비스 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 타임아웃 관련 헤더 (선택사항)
        headers.set("X-Request-Timeout", String.valueOf(sessionTimeout));

        return headers;
    }

    private String generateSessionId() {
        // 사용자 식별 가능한 세션 ID 생성 (실제로는 사용자 ID 등을 포함할 수 있음)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return "session_" + timestamp + "_" + uuid;
    }

    private String createErrorResponse(String message) {
        return String.format(
                "{\"success\":false,\"answer\":\"%s\",\"conversation_context\":null,\"sources\":[],\"error\":\"true\"}",
                message
        );
    }

    // ========== 추가 유틸리티 메서드들 ==========

    /**
     * 질문의 유효성을 검사합니다.
     */
    private boolean isValidQuestion(String question) {
        return question != null
                && !question.trim().isEmpty()
                && question.length() <= 1000; // 최대 1000자 제한
    }

    /**
     * 응답이 유효한지 검사합니다.
     */
    private boolean isValidResponse(String response) {
        return response != null
                && !response.trim().isEmpty()
                && response.contains("success");
    }

    /**
     * 디버그용 - 현재 설정된 MCP 서버 정보를 로그로 출력
     */
    public void logMcpConfiguration() {
        log.info("=== MCP 서비스 설정 정보 ===");
        log.info("서버 URL: {}", mcpServerUrl);
        log.info("데이터베이스: {}", databaseName);
        log.info("벡터 스토어: {}", vectorStoreName);
        log.info("세션 타임아웃: {}초", sessionTimeout);
        log.info("서비스 상태: {}", isServiceHealthy() ? "정상" : "비정상");
        log.info("========================");
    }
}