package pard.server.com.portfolioarchiveback.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.portfolioarchiveback.portfolio.Portfolio;
import java.util.List;
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByPortfolioIdAndIsThumbnail(Long portfolioId, boolean isThumbnail);
}