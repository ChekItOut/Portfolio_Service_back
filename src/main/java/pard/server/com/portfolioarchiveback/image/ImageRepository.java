package pard.server.com.portfolioarchiveback.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.portfolioarchiveback.portfolio.Portfolio;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}