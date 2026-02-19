package pard.server.com.portfolioarchiveback.skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.portfolioarchiveback.description.Description;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;

    public void save(Long portfolioId, List<String> skillList) { //모든 기술스택 엔티티변환 후 일괄 저장
        List<Skill> skills = skillList.stream()
                .map(skillName -> Skill.builder()
                        .portfolioId(portfolioId)
                        .skillName(skillName)
                        .build())
                .toList();
        skillRepository.saveAll(skills);
    }
}
