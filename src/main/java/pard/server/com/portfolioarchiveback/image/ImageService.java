package pard.server.com.portfolioarchiveback.image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.portfolioarchiveback.s3.AwsS3Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    public final ImageRepository imageRepository;
    private final AwsS3Service awsS3Service;

    @Transactional
    public void uploadImage(Long portfolioId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("이미지가 없습니다.");
        }

        for(int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if(file.isEmpty()) continue; //해당 리스트의 파일이 없으면 다음 리스트로 스킵

            String fileName = awsS3Service.uploadFile(file); //s3에 파일 업로드 후 파일 이름 리턴 받음

            boolean isThumb = (i == 0); //첫번째 사진이라 썸네일인지 판단
            Image image = Image.builder()
                    .fileName(fileName)
                    .isThumbnail(isThumb)
                    .portfolioId(portfolioId)
                    .build();
            imageRepository.save(image);
        }
    }

    public String getThumbURL(Long portfolioId) {
        Image image = imageRepository.findByPortfolioIdAndIsThumbnail(portfolioId, true)
                .orElseThrow(() -> new RuntimeException("Thumbnail image not found for portfolio id: " + portfolioId));

        return awsS3Service.getFileUrl(image.getFileName());
    }

    @Transactional
    public void deleteImage(Long portfolioId) {
        List<Image> imageList = imageRepository.findAllByPortfolioId(portfolioId);
        imageList.forEach(image -> { //s3에서 모든 파일 삭제
            awsS3Service.deleteFile(image.getFileName());
        });
        imageRepository.deleteByPortfolioId(portfolioId); //DB에서 유지중인 모든 file이름 정보 삭제
    }

    public void addImage(Long portfolioId, List<MultipartFile> imageList) {
        for(int i = 0; i < imageList.size(); i++) {
            MultipartFile file = imageList.get(i);
            if(file.isEmpty()) continue;

            String fileName = awsS3Service.uploadFile(file); // s3에 파일 업로드 후 파일 이름 리턴 받음

            Image image = Image.builder()
                    .fileName(fileName)
                    .isThumbnail(false) //썸네일이 아니니 false로 설정
                    .portfolioId(portfolioId)
                    .build();
            imageRepository.save(image);
        }
    }
}
