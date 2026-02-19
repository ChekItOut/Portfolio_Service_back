package pard.server.com.portfolioarchiveback.config.jwt.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreateAccessTokenResponse {
    private String accessToken;
}
