package pard.server.com.portfolioarchiveback.portfolio;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.portfolioarchiveback.baseEntity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Portfolio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    private Long userId;

    private String title;
}
