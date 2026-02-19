package pard.server.com.portfolioarchiveback.user;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.portfolioarchiveback.baseEntity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    private String email;
}
