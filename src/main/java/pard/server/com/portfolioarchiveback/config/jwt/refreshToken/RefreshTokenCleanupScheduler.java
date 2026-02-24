package pard.server.com.portfolioarchiveback.config.jwt.refreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 만료된 RefreshToken을 주기적으로 삭제하는 스케줄러
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 매일 새벽 3시에 만료된 RefreshToken 삭제
     * cron: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredTokens() {
        log.info("RefreshToken 정리 시작...");

        LocalDateTime now = LocalDateTime.now();

        // 삭제 전 개수 확인 (로깅용)
        long expiredCount = refreshTokenRepository.countByExpiryDateBefore(now);

        if (expiredCount > 0) {
            // 만료된 토큰 삭제
            refreshTokenRepository.deleteByExpiryDateBefore(now);
            log.info("{}개의 만료된 RefreshToken을 삭제했습니다", expiredCount);
        } else {
            log.info("삭제할 만료된 RefreshToken이 없습니다");
        }
    }
}
