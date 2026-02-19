package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.portfolioarchiveback.description.DescriptionRepository;
import pard.server.com.portfolioarchiveback.image.ImageRepository;
import pard.server.com.portfolioarchiveback.image.ImageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final ImageService imageService;

    public List<PortfolioDTO.Res1> getPortfolio(Long userId) { //히어로섹션 갤러리들 모두 리턴
        List<Portfolio> portfolios = portfolioRepository.findAllByUserId(userId);

        return portfolios.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .imageURL(imageService.getThumbURL(portfolio.getPortfolioId())) //해당 포폴의 썸네일 추출
                        .title(portfolio.getTitle()) //제목 추출
                        .build())
                .toList(); //모두 리스트로 담아서 리턴
    }

    public Long createPortfolio(Long userId, String title) { //포폴생성
        Portfolio portfolio = Portfolio.builder()
                .title(title)
                .userId(userId)
                .build();
        portfolioRepository.save(portfolio);

        return portfolio.getPortfolioId();
    }
}
