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
        Image image = imageRepository.findByPortfolioIdAndIsThumbnail(portfolioId, true);

        return awsS3Service.getFileUrl(image.getFileName());
    }
}
