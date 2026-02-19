package pard.server.com.portfolioarchiveback.description;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.portfolioarchiveback.portfolio.Portfolio;

@Repository
public interface DescriptionRepository extends JpaRepository<Description, Long> {
}