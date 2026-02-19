package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.portfolioarchiveback.description.DescriptionService;
import pard.server.com.portfolioarchiveback.image.ImageService;
import pard.server.com.portfolioarchiveback.skill.SkillService;
import pard.server.com.portfolioarchiveback.user.AuthorizeUserId;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final DescriptionService descriptionService;
    private final SkillService skillService;
    private final ImageService imageService;

    @GetMapping("") //히어로섹션에서 보여줄 모든 갤러리와 갤러리 클릭시 나오는 제목
    public ResponseEntity<List<PortfolioDTO.Res1>> getPortfolio() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        List<PortfolioDTO.Res1> result = portfolioService.getPortfolio(myId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //포폴 게시글 작성
    public ResponseEntity<?> createPortfolio( @RequestPart("request") PortfolioDTO.Req1 request, @RequestPart("images") List<MultipartFile> images) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();

        //포폴을 구성하는 각각의 구성요소들을 소분하여 저장
        Long portfolioId = portfolioService.createPortfolio(myId, request.getTitle()); //포폴 생성
        descriptionService.save(portfolioId, request.getDescription()); //설명글 저장
        skillService.save(portfolioId, request.getSkill()); //스킬 저장
        imageService.uploadImage(portfolioId, request.getImage()); //사진들 업로드

        return ResponseEntity.ok().build();
    }
}
