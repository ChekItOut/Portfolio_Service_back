package pard.server.com.portfolioarchiveback.config.oauth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pard.server.com.portfolioarchiveback.config.jwt.TokenProvider;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshToken;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshTokenRepository;
import pard.server.com.portfolioarchiveback.user.UserService;
import pard.server.com.portfolioarchiveback.user.User;

import pard.server.com.portfolioarchiveback.util.CookieUtil;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofHours(1);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);
    public static final String REDIRECT_MAINPAGE = "http://localhost:3000/oauth/callback";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;

    @Override //request, response는 사용자가 “구글 로그인”을 끝내고 우리 서버로 돌아온 그 HTTP 요청/응답 객체야.
    //OAuth2 로그인에 성공하면 Spring Security가 이 메서드를 호출
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); //authentication.getPrincipal()에는 구글에서 가져온 사용자 정보(OAuth2User) 가 들어있음
        User user = userService.findByEmail((String) oAuth2User.getAttributes().get("email")); //그 안의 email로 우리 DB의 User 엔티티를 조회해옴

        //Refresh Token 발급 → DB 저장 → 쿠키 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getUserId(), refreshToken); //DB에 “이 유저의 refresh 토큰” 저장(또는 갱신)
        addRefreshTokenToCookie(request, response, refreshToken); //브라우저에 HttpOnly 쿠키로 refresh 토큰 저장

        //인증 관련 설정값, 쿠키 제거 = OAuth2 로그인 과정 중 사용했던 “인증 관련 임시 데이터”를 삭제
        //authorizationRequest를 쿠키에 저장했으니 그 쿠키를 제거함
        clearAuthenticationAttributes(request, response);

        //리다이렉트로 프론트(또는 특정 페이지)로 보내기
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_MAINPAGE);
        //브라우저에게 302 응답을 줘서 targetUrl로 이동시키는 단계
    }

    //생성된 리프레시 토큰을 전달받아 DB에 저장
    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    //브라우저에 HttpOnly 쿠키로 refresh 토큰 저장(
    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        //쿠키 만료 시간을 refresh 토큰 기간과 동일하게 맞춤
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        //기존 refresh 쿠키 제거 후 새로 세팅
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        //인증 과정에서 생긴 임시 속성들을 정리(기본 제공 로직)
        super.clearAuthenticationAttributes(request);
        //너가 쿠키에 저장해둔 OAuth2AuthorizationRequest(로그인 중간 state 등)를 제거
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}