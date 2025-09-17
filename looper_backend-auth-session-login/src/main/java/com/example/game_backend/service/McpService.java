package com.example.game_backend.service;

public interface McpService {

    /**
     * 기본 질문 (새로운 세션으로 시작)
     */
    String askMcp(String question);

    /**
     * 세션 ID를 지정하여 질문 (대화 맥락 유지)
     */
    String askMcp(String question, String sessionId);

    /**
     * 특정 세션의 대화 기록 삭제
     */
    void clearConversation(String sessionId);

    /**
     * MCP 서비스 상태 확인
     */
    boolean isServiceHealthy();
}