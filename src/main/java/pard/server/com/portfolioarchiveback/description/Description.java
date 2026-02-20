package pard.server.com.portfolioarchiveback.description;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Description {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long descriptionId;

    private Long portfolioId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String context;
}
