package pard.server.com.portfolioarchiveback.config.jwt.token;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccessTokenRequest {
    private String refreshToken;
}