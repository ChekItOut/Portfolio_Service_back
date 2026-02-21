package pard.server.com.portfolioarchiveback.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.portfolioarchiveback.description.DescriptionService;
import pard.server.com.portfolioarchiveback.image.ImageService;
import pard.server.com.portfolioarchiveback.skill.SkillService;
import pard.server.com.portfolioarchiveback.user.AuthorizeUserId;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final DescriptionService descriptionService;
    private final SkillService skillService;
    private final ImageService imageService;

    @GetMapping("/hero") //히어로섹션에서 보여줄 모든 갤러리와 갤러리 클릭시 나오는 제목
    public ResponseEntity<List<PortfolioDTO.Res1>> getPortfolios() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        List<PortfolioDTO.Res1> result = portfolioService.getPortfolios(myId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/detail/{portfolioId}") // 포폴 상세 페이지
    public ResponseEntity<PortfolioDTO.Res2> getPortfolioDetail(@PathVariable Long portfolioId) {
        PortfolioDTO.Res2 result = portfolioService.getPortfolioDetail(portfolioId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //포폴 게시글 작성
    @Transactional
    public ResponseEntity<?> createPortfolio( @RequestPart("request") PortfolioDTO.Req1 request, @RequestPart("images") List<MultipartFile> images) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();

        //포폴을 구성하는 각각의 구성요소들을 소분하여 저장
        Long portfolioId = portfolioService.createPortfolio(myId, request.getTitle()); //포폴 생성
        descriptionService.save(portfolioId, request.getDescription()); //설명글 저장
        skillService.save(portfolioId, request.getSkill()); //스킬 저장
        imageService.uploadImage(portfolioId, images); //사진들 업로드

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/text/{portfolioId}") // 포트폴리오 수정에서 글자 기반 정보만 수정했을 때
    public ResponseEntity<Void> updatePortfolioText(@PathVariable Long portfolioId, @RequestBody PortfolioDTO.Req2 request) {
        portfolioService.updatePortfolioText(portfolioId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value="/imageReorder/{portfolioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 포트폴리오 수정에서 사진 정보만 수정했을 때 (순서가 바뀌었을 때)
    public ResponseEntity<Void> updatePortfolioImageReorder(@PathVariable Long portfolioId, @RequestPart("images") List<MultipartFile> images) {
        imageService.deleteImage(portfolioId); // 기존 사진들 모두 삭제
        imageService.uploadImage(portfolioId, images); //사진 다시 업로드
        return ResponseEntity.ok().build();
    }

    // 포트폴리오 수정에서 사진 정보만 수정했을 때 (기존 사진 그대로에 새로운 사진 추가했을때)
    @PostMapping(value="/imageAdd/{portfolioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePortfolioImageAdd(@PathVariable Long portfolioId, @RequestPart("images") List<MultipartFile> images) {
        imageService.addImage(portfolioId, images); //사진 추가
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.ok().build();
    }
}
