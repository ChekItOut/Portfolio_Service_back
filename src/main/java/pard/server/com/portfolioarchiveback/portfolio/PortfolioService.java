package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.portfolioarchiveback.image.ImageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final ImageRepository imageRepository;

    /*public List<PortfolioDTO.Res1> getPortfolio(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findAllByUserId(userId);

        return portfolios.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .imageURL())
    }*/
}
