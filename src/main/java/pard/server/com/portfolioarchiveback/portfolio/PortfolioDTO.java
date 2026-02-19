package pard.server.com.portfolioarchiveback.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PortfolioDTO {
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 { //
        private String imageURL;

        private String title;
    }
}
