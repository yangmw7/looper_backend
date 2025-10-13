package com.example.game_backend.interceptor;

import com.example.game_backend.repository.PenaltyRepository;
import com.example.game_backend.repository.entity.Penalty;
import com.example.game_backend.repository.entity.PenaltyType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 제재 중인 사용자의 접근을 제한하는 인터셉터
 */
@Component
@RequiredArgsConstructor
public class PenaltyCheckInterceptor implements HandlerInterceptor {

    private final PenaltyRepository penaltyRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 로그인하지 않은 사용자는 통과
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return true;
        }

        String username = auth.getName();

        // 현재 활성 제재 조회
        List<Penalty> activePenalties = penaltyRepository
                .findByMember_UsernameAndIsActiveTrueOrderByCreatedAtDesc(username);

        // 만료된 제재 비활성화
        LocalDateTime now = LocalDateTime.now();
        activePenalties.forEach(penalty -> {
            if (penalty.getEndDate() != null && penalty.getEndDate().isBefore(now)) {
                penalty.setIsActive(false);
            }
        });

        // 여전히 활성인 제재가 있는지 확인
        boolean hasActivePenalty = activePenalties.stream()
                .anyMatch(Penalty::getIsActive);

        if (!hasActivePenalty) {
            return true; // 제재 없음, 통과
        }

        // 가장 강력한 제재 찾기
        Penalty strongestPenalty = activePenalties.stream()
                .filter(Penalty::getIsActive)
                .max((p1, p2) -> comparePenaltyType(p1.getType(), p2.getType()))
                .orElse(null);

        if (strongestPenalty == null) {
            return true;
        }

        // 제재 유형별 처리
        switch (strongestPenalty.getType()) {
            case WARNING:
                // 경고는 기능 제한 없음 (기록만)
                return true;

            case SUSPENSION:
                // 정지는 특정 기능 제한
                String method = request.getMethod();
                String uri = request.getRequestURI();

                // POST, PUT, DELETE 요청 차단
                if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
                    // 단, 이의신청은 허용
                    if (uri.contains("/appeal")) {
                        return true;
                    }

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(String.format(
                            "{\"error\":\"계정이 정지되었습니다.\",\"reason\":\"%s\",\"endDate\":\"%s\"}",
                            strongestPenalty.getReason(),
                            strongestPenalty.getEndDate()
                    ));
                    return false;
                }
                return true;

            case PERMANENT:
                // 영구정지는 모든 기능 차단
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(String.format(
                        "{\"error\":\"계정이 영구정지되었습니다.\",\"reason\":\"%s\"}",
                        strongestPenalty.getReason()
                ));
                return false;
        }

        return true;
    }

    /**
     * 제재 유형 비교 (숫자가 클수록 강력함)
     */
    private int comparePenaltyType(PenaltyType t1, PenaltyType t2) {
        return getPenaltyWeight(t1) - getPenaltyWeight(t2);
    }

    private int getPenaltyWeight(PenaltyType type) {
        return switch (type) {
            case WARNING -> 1;
            case SUSPENSION -> 2;
            case PERMANENT -> 3;
        };
    }
}