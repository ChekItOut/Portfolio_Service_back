package pard.server.com.portfolioarchiveback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class PortfolioArchiveBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioArchiveBackApplication.class, args);
    }

}
