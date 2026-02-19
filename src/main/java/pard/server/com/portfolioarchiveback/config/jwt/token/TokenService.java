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
            throw new IllegalArgumentException("Unexpected token");
        }

        //리프레시 토큰의 주인인 유저를 찾고
        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        //찾은 유저로 새로운 AccessToken 생성
        return tokenProvider.generateToken(user, Duration.ofHours(2));
    }
}