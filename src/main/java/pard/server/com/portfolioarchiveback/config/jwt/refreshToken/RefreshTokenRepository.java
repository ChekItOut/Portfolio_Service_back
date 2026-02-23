package pard.server.com.portfolioarchiveback.config.jwt.refreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    void deleteByRefreshToken(String refreshToken);

    // 만료된 토큰 삭제
    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime currentTime);

    // 만료된 토큰 개수 조회 (로깅용)
    long countByExpiryDateBefore(LocalDateTime currentTime);
}