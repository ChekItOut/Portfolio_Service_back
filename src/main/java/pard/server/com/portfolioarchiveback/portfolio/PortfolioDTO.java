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
    public static class Req1 { // 포폴 등록 post
        private String title;
        private List<String> description;
        private List<String> skill;
        private List<MultipartFile> image;
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Req2 { // 포폴 text기반 정보만 수정
        private String title;
        private List<String> description;
        private List<String> skill;
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 { // 히어로섹션
        private Long portfolioId;
        private String imageURL;
        private String title;
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res2 { // 포폴 상세페이지
        private String title;
        private List<String> description;
        private List<String> skill;
        private List<String> imageURL;
    }
}
