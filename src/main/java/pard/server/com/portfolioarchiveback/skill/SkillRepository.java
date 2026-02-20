package pard.server.com.portfolioarchiveback.skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.portfolioarchiveback.portfolio.Portfolio;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByPortfolioId(Long portfolioId);

    void deleteAllByPortfolioId(Long portfolioId);
}