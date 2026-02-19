package pard.server.com.portfolioarchiveback.util;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;

public class CookieUtil {
    //요청값 (이름, 만료 기간)을 바탕으로 http응답에 쿠키 추가
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge){
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    //쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        //실제로 쿠키를 삭제하는 방벙은 없어서
        //파라미터로 넘어온 키의 쿠키를 빈 값으로 바꾸고 만료 시간을 0으로 설정해 쿠키가 생성되자마자 만료처리한다.
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    //객체를 직렬화해 쿠키의 값에 들어갈 값으로 변환
    //구글 로그인 중간 과정에서 서버가 기억해야할 것들이 있다. state, redirect_uri.
    //기존에는 세션에 저장했지만 이번에는 쿠키에 저장
    //쿠키에 직렬화해서 저장/복원하겠다
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    //쿠키를 역직렬화해 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }

    //쿠키에서 refreshToken 추출
    public static String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "refresh_token"); // 쿠키 이름 맞춰줘
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return null;
        }
        return cookie.getValue();
    }
}
