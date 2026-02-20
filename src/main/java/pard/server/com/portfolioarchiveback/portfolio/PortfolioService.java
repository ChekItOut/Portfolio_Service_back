package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.portfolioarchiveback.description.Description;
import pard.server.com.portfolioarchiveback.description.DescriptionRepository;
import pard.server.com.portfolioarchiveback.description.DescriptionService;
import pard.server.com.portfolioarchiveback.image.Image;
import pard.server.com.portfolioarchiveback.image.ImageRepository;
import pard.server.com.portfolioarchiveback.image.ImageService;
import pard.server.com.portfolioarchiveback.s3.AwsS3Service;
import pard.server.com.portfolioarchiveback.skill.Skill;
import pard.server.com.portfolioarchiveback.skill.SkillRepository;
import pard.server.com.portfolioarchiveback.skill.SkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final ImageService imageService;
    private final SkillRepository skillRepository;
    private final DescriptionRepository descriptionRepository;
    private final ImageRepository imageRepository;
    private final AwsS3Service awsS3Service;
    private final DescriptionService descriptionService;
    private final SkillService skillService;

    public List<PortfolioDTO.Res1> getPortfolios(Long userId) { //히어로섹션 갤러리들 모두 리턴
        List<Portfolio> portfolios = portfolioRepository.findAllByUserId(userId);

        return portfolios.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .portfolioId(portfolio.getPortfolioId())
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

    public PortfolioDTO.Res2 getPortfolioDetail(Long portfolioId) { //포폴 상세페이지 정보리턴
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));

        List<Skill> skillList = skillRepository.findAllByPortfolioId(portfolioId);
        List<String> skillNameList = skillList.stream()
                .map(Skill::getSkillName)
                .toList();

        List<Description> descriptionList = descriptionRepository.findAllByPortfolioId(portfolioId);
        List<String> contextList = descriptionList.stream()
                .map(Description::getContext)
                .toList();

        List<Image> imageList = imageRepository.findAllByPortfolioId(portfolioId);
        List<String> imageURLs = imageList.stream()
                .map(img -> awsS3Service.getFileUrl(img.getFileName()))
                .toList();

        PortfolioDTO.Res2 response =  PortfolioDTO.Res2.builder()
                .title(portfolio.getTitle())
                .skill(skillNameList)
                .description(contextList)
                .imageURL(imageURLs)
                .build();

        return response;
    }

    @Transactional
    public void updatePortfolioText(Long portfolioId, PortfolioDTO.Req2 request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portfolioId));
        portfolio.updateTitle(request.getTitle()); //제목 수정

        descriptionService.deleteAll(portfolioId); //기존 포폴의 설명글 모두 삭제
        descriptionService.save(portfolioId, request.getDescription()); //새로운 설명글들 저장

        skillService.deleteAll(portfolioId); //기존 포폴의 스킬들 모두 삭제
        skillService.save(portfolioId, request.getSkill());
    }

    public void deletePortfolio(Long portfolioId) {
        descriptionService.deleteAll(portfolioId);
        skillService.deleteAll(portfolioId);

        List<Image> imageList = imageRepository.findAllByPortfolioId(portfolioId);
        imageList.forEach(image -> { //s3에서 모든 파일 삭제
            awsS3Service.deleteFile(image.getFileName());
        });

        portfolioRepository.deleteById(portfolioId);
    }
}
