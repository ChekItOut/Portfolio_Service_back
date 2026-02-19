package pard.server.com.portfolioarchiveback.config.jwt;


import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pard.server.com.portfolioarchiveback.config.jwt.token.CustomPrincipal;
import pard.server.com.portfolioarchiveback.user.User;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
//토큰을 생성하고 올바른 토큰인지 유효성 검사를 하고, 토큰에서 필요한 정보를 가져오는 클래스
public class TokenProvider {

    private final JwtProperties jwtProperties;

    //JWT 토큰 생성 매서드
    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiredAt.toMillis());
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) //헤더 typ: JWT
                .setIssuer(jwtProperties.getIssuer()) //yaml에 설정한 issuer값
                .setIssuedAt(now) //iat: 현재 시간
                .setExpiration(expiry) // expiry: 멤버 변수값
                .setSubject(user.getEmail()) //sub: 유저의 이메일
                .claim("userId", user.getUserId()) //클레임 id: 유저 id
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    //JWT 토큰 유효성 검증 메서드
    public boolean validToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //토큰을 받아 인증벙보를 담은 객체 authentication을 반환하는 메서드
    //JWT 필터가 받아서 SecurityContext에 꽂아 넣는 데 쓰여.
    //그 다음부터는 스프링 시큐리티가 “이 요청은 인증된 사용자 요청”으로 취급해.
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        var authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject(); // subject에 email 넣었으니까

        var principal = new CustomPrincipal(userId, email);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    //토큰 기반으로유저 id를 가져오는 메서드
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    //StompHandler에서 사용하는 auth에서 Bearer를 제거하고 JWT만 리턴하는 메서드
    public String substringToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header is empty.");
        }
        final String BEARER = "Bearer ";

        if (!authorizationHeader.startsWith(BEARER)) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '.");
        }

        String jwt = authorizationHeader.substring(BEARER.length()).trim();

        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("JWT is missing after 'Bearer '.");
        }

        return jwt;
    }
}