package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pard.server.com.portfolioarchiveback.user.AuthorizeUserId;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping("") //히어로섹션에서 보여줄 모든 갤러리와 갤러리 클릭시 나오는 제목
    public List<PortfolioDTO.Res1> getPortfolio() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return portfolioService.getPortfolio(myId);
    }
}
