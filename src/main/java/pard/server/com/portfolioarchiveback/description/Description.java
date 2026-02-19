package pard.server.com.portfolioarchiveback.description;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.portfolioarchiveback.baseEntity.BaseEntity;

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

    private String context;
}
