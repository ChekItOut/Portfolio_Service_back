package pard.server.com.portfolioarchiveback.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pard.server.com.portfolioarchiveback.config.jwt.TokenProvider;
import pard.server.com.portfolioarchiveback.config.jwt.refreshToken.RefreshTokenRepository;
import pard.server.com.portfolioarchiveback.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import pard.server.com.portfolioarchiveback.config.oauth.OAuth2SuccessHandler;
import pard.server.com.portfolioarchiveback.config.oauth.OAuth2UserCustomService;
import pard.server.com.portfolioarchiveback.user.UserService;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebOAuthSecurityConfig {
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                // ✅ Spring Boot가 기본으로 제공하는 정적 리소스 위치들( /static, /public, /resources, /META-INF/resources )
                PathRequest.toStaticResources().atCommonLocations()
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //✅ RequestMatcher를 PathPattern 기반으로 통일 (최신 권장)
        var apiToken = PathPatternRequestMatcher.withDefaults().matcher("/api/token");
        var swaggerUi = PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**");
        var swaggerHtml = PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui.html");
        var apiDocs = PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**");

        return http //토큰 방식으로 인증하기 때문에 기존 폼 로그인, 세션 기능을 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ 추가
                //직접 만든 헤더를 확인 할 필터를 추가
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                .csrf(csrf -> csrf
                        .disable()
                )

                // ✅ authorizeRequests -> authorizeHttpRequests 로 변경
                .authorizeHttpRequests(auth -> auth
                        // ✅ 1) OAuth2 로그인 흐름에 필요한 엔드포인트는 열어줘야 함
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // ✅ 2) “로그인 없이 허용”하려는 API들
                        .requestMatchers(apiToken).permitAll()
                        .requestMatchers(swaggerUi, swaggerHtml, apiDocs).permitAll() //스웨거 허용
                        .requestMatchers(HttpMethod.DELETE, "/api/refresh-token").permitAll()
                        // ✅ 3) 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                                //OAuth2 로그인 기능을 활성화하고, 그 로그인 과정에서 사용할 세부 옵션들을 설정하는 곳이야
                                .authorizationEndpoint(ae -> ae
                                        .authorizationRequestRepository(
                                                oAuth2AuthorizationRequestBasedOnCookieRepository()
                                        )//“OAuth2 로그인 과정에서 OAuth2AuthorizationRequest(state 등 포함) 를 세션 대신 쿠키에 저장/복원하기 위한 저장소”
                                )
                                .userInfoEndpoint(uie -> uie //구글 로그인 성공 후
                                        .userService(oAuth2UserCustomService)//사용자 정보를 가져오는데, 그 “사용자 정보 가져온 뒤 처리”를 담당하는 서비스
                                )
                                .successHandler(oAuth2SuccessHandler()) //OAuth2 로그인에 성공했을 때 실행되는 로직
                        //로그인을 성공하면 JWT(AccessToken, RefreshToken)을 발급해야하기 때문에 이를 발급하는 로직
                        //JWT(Access/Refresh)를 발급하고, refresh는 쿠키+DB에 저장한 다음 프론트로 리다이렉트까지 해주는 “마무리 담당”
                )

                //인증 실패 시 401 상태코드를 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService
        );
    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }


    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트 주소
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ 쿠키(refreshToken) 쓰면 필수
        config.setExposedHeaders(List.of("Authorization")); // 필요시 노출

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}