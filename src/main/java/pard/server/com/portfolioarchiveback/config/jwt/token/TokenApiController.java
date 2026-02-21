package pard.server.com.portfolioarchiveback.config.jwt.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshTokenService;
import pard.server.com.portfolioarchiveback.util.CookieUtil;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class TokenApiController {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/token") //새로운 AccessToken을 만들어달라는 요청
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(HttpServletRequest request) { //쿠키에서 RefreshToken 추출
        // 쿠키에서 RefreshToken 추출
        String refreshToken = CookieUtil.extractRefreshTokenFromCookie(request);

        // RefreshToken이 없거나 빈 문자열인 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = tokenService.createNewAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED) //새로만든 AccessToken을 리턴
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @DeleteMapping("/refresh-token") //로그아웃시에 refreshToken삭제 및 쿠키 만료설정
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1) 요청 쿠키에서 refresh_token 꺼내기
        String refreshToken = CookieUtil.extractRefreshTokenFromCookie(request);

        try {
            if (refreshToken != null && !refreshToken.isBlank()) {// 2) DB/Redis에서 refreshToken 삭제(또는 해당 유저 세션 삭제)
                refreshTokenService.deleteByRefreshToken(refreshToken);
            }
        } catch (Exception e) {
            log.warn("Logout: failed to delete refresh token from store", e);
        } finally {// 3) 쿠키 만료로 삭제
            CookieUtil.deleteCookie(request, response, "refresh_token"); // ✅ 무조건 실행
        }
        return ResponseEntity.ok().build();

    }
}