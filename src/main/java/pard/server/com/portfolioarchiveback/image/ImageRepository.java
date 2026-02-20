package pard.server.com.portfolioarchiveback.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.portfolioarchiveback.portfolio.Portfolio;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByPortfolioIdAndIsThumbnail(Long portfolioId, boolean isThumbnail);

    List<Image> findAllByPortfolioId(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}