package nl.chrisregenboog.kvkassessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class KvkAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KvkAssessmentApplication.class, args);
    }

}
