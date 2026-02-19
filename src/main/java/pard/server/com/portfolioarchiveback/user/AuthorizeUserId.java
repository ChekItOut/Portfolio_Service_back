package pard.server.com.portfolioarchiveback.user;

import org.springframework.security.core.context.SecurityContextHolder;
import pard.server.com.portfolioarchiveback.config.jwt.token.CustomPrincipal;

public class AuthorizeUserId {
    public static Long getAuthorizedUserId(){
        CustomPrincipal p = (CustomPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return p.userId();
    }
}
