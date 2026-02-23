package pard.server.com.portfolioarchiveback.config.jwt.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.portfolioarchiveback.config.jwt.TokenProvider;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshTokenService;
import pard.server.com.portfolioarchiveback.user.User;
import pard.server.com.portfolioarchiveback.user.UserService;


import java.time.Duration;

@RequiredArgsConstructor
@Service
//리프레시 토큰을 전달받아 토큰 유효성 검사를 진행하고, 유효한 토큰일 때 새로운 AccessToken을 생성
public class TokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    //리프레시 토큰을 전달받음
    public String createNewAccessToken(String refreshToken) {
        if(!tokenProvider.validToken(refreshToken)) { //유효성 체크
            // 만료된 토큰을 DB에서도 삭제 (만료된 토큰 즉시 정리)
            try {
                refreshTokenService.deleteByRefreshToken(refreshToken);
            } catch (Exception e) {
                // 이미 삭제되었거나 존재하지 않는 경우 무시
            }
            throw new IllegalArgumentException("Unexpected token");
        }

        //리프레시 토큰의 주인인 유저를 찾고
        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        //찾은 유저로 새로운 AccessToken 생성
        return tokenProvider.generateToken(user, Duration.ofMinutes(15));//AccessToken 만료 기간설정
    }
}