package pard.server.com.portfolioarchiveback.config.jwt.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshTokenService;
import pard.server.com.portfolioarchiveback.util.CookieUtil;


@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class TokenApiController {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/token") //새로운 AccessToken을 만들어달라는 요청
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request) { //DTO
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED) //새로만든 AccessToken을 리턴
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @DeleteMapping("/refresh-token") //로그아웃시에 refreshToken삭제 및 쿠키 만료설정
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1) 요청 쿠키에서 refresh_token 꺼내기
        String refreshToken = CookieUtil.extractRefreshTokenFromCookie(request);
        // 2) DB/Redis에서 refreshToken 삭제(또는 해당 유저 세션 삭제)
        refreshTokenService.deleteByRefreshToken(refreshToken);
        // 3) 쿠키 만료로 삭제
        CookieUtil.deleteCookie(request, response, "refresh_token");

        return ResponseEntity.ok().build();

    }
}