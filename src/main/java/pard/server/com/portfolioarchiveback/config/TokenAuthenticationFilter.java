package pard.server.com.portfolioarchiveback.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pard.server.com.portfolioarchiveback.config.jwt.TokenProvider;
import pard.server.com.portfolioarchiveback.config.jwt.token.CustomPrincipal;


import java.io.IOException;
@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter { //OncePerRequestFilter요청 1번 마다 1번씩 실행되도록
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    //요청이 컨롤러에 들어가기 전에, 매번 요청마다 실행되는 필터이고, JWT토큰을 꺼내서 검증하고 맞으면 이번 요청의 로그인 상태(인증정보)를
    //SecurityContext에 심는다. 누구인지 확인하는 과정
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String token = getAccessToken(authorizationHeader); //토큰 추출

        log.info("[JWT] {} {} authHeader={}",
                request.getMethod(), request.getRequestURI(), authorizationHeader);

        if (token == null) {
            log.info("[JWT] token is null -> skip auth");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("[JWT] tokenPrefix={}", token.length() >= 12 ? token.substring(0, 12) : token);

        boolean valid = tokenProvider.validToken(token);
        log.info("[JWT] validToken={}", valid);

        if (valid) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (authentication.getPrincipal() instanceof CustomPrincipal p) {
                log.info("[JWT] authentication set. userId={}, email={}", p.userId(), p.email());
            } else {
                log.info("[JWT] authentication set. principalType={}", authentication.getPrincipal().getClass().getName());
            }
        }

        //만약 유효하지 않은 accessToken이면 authentication도 생성안되고 SecurityContext에저장도 안됨
        //모든 필터들은 Spring Security에 존재하는데, 인증정보가 없으면 Spring Security의 “인가 단계”에서 막혀서 401이 발생하여
        //프론트에게 RefreshToken을 사용해 새로운 AccessToken을 발급하라고 알린다.
        //예를 들어 /posts/my 같은 API가 .authenticated() 또는 hasRole("USER")로 보호되어 있으면:
        //현재 SecurityContext에 인증이 없음(익명) -> 근데 이 URL은 인증 필요
        //Spring Security가 401 Unauthorized를 반환하고 컨트롤러 호출 자체가 안 됨

        filterChain.doFilter(request, response); //다음 필터로 요청 전달
        //모든 필터가 끝나야지 요청이 컨트롤러로 간다
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
