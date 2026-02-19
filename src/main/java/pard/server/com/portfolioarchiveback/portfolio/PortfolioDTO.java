package pard.server.com.portfolioarchiveback.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PortfolioDTO {
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Req1 { //
        private String title;
        private List<String> description;
        private List<String> skill;
        private List<MultipartFile> image;
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 { //
        private String imageURL;

        private String title;
    }
}
