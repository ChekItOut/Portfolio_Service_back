package pard.server.com.portfolioarchiveback.image;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private Long portfolioId;

    private String fileName;

    private boolean isThumbnail;
}
