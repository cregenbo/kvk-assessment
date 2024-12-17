package nl.chrisregenboog.kvkassessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = KvkAssessmentApplication.class)
class KvkAssessmentApplicationTests {

    public static final String NETHERLANDS = "Netherlands";
    public static final String NL_POSTAL_CODE_FORMAT = "#### @@";
    public static final String NL_POSTAL_CODE_REGEX = "^(\\d{4}[A-Z]{2})$";

    @Autowired
    CountryInfoAPIController countryInfoAPIController;

    @Autowired
    CountryInfoRepository countryInfoRepository;

    WebTestClient webTestClient;

    @BeforeEach
    public void setup() {

        // Setup webTestClient
        webTestClient = WebTestClient.bindToController(countryInfoAPIController).build();

        // Clear db
        countryInfoRepository.deleteAll();
    }

    @Test
    @DisplayName("The Netherlands country code should be added to the database after a post request")
    public void postCountryCodeTest() {
        // Post nl country
        webTestClient.post().uri("/nl").exchange()
                .expectStatus().isOk();

        // Check if nl country is in db
        countryInfoRepository.findByCountryCode("nl")
                .ifPresentOrElse(
                        countryInfo -> {
                            assertEquals(NETHERLANDS, countryInfo.getCountryName());
                            assertEquals(NL_POSTAL_CODE_FORMAT, countryInfo.getPostalCodeFormat());
                            assertEquals(NL_POSTAL_CODE_REGEX, countryInfo.getPostalCodeRegex());
                        },
                        () -> {
                            throw new AssertionError("Country nl not found in db");
                        }
                );
    }

    @Test
    @DisplayName("The Netherlands country info should be returned after a get request, if present in the database")
    public void getExistingCountryCodeTest() {
        // Populate db with nl country
        webTestClient.post().uri("/nl").exchange()
                .expectStatus().isOk();

        // Get nl country info
        webTestClient.get().uri("/nl").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.countryName").isEqualTo(NETHERLANDS)
                .jsonPath("$.postalCodeFormat").isEqualTo(NL_POSTAL_CODE_FORMAT)
                .jsonPath("$.postalCodeRegex").isEqualTo(NL_POSTAL_CODE_REGEX);
    }

    @Test
    @DisplayName("A 204 No Content status should be returned after a get request, if the country code is not present in the database")
    public void getAbsentCountryCodeTest() {
        webTestClient.get().uri("/foobar").exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("A 500 Internal Server Error status should be returned after a post request, if the country code is not present in the external API")
    public void postAbsentCountryCodeTest() {
        webTestClient.post().uri("/foobar").exchange()
                .expectStatus().is5xxServerError();
    }
}
