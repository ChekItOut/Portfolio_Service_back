package pard.server.com.portfolioarchiveback.description;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DescriptionService {
    private final DescriptionRepository descriptionRepository;

    public void save(Long portfolioId, List<String> descriptionList) { //모든 설명글 엔티티변환 후 일괄 저장
        List<Description> descriptions = descriptionList.stream()
                .map(context -> Description.builder()
                        .portfolioId(portfolioId)
                        .context(context)
                        .build())
                .toList();
        descriptionRepository.saveAll(descriptions);
    }
}
