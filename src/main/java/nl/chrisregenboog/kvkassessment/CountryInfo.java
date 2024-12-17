package nl.chrisregenboog.kvkassessment;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Country information")
public class CountryInfo {

    @Id
    @Schema(description = "The country code", example = "NL")
    String countryCode;

    @JsonView(PublicView.class)
    @Schema(description = "The country name", example = "Netherlands")
    String countryName;

    @JsonView(PublicView.class)
    @Schema(description = "The postal code format", example = "#### @@")
    String postalCodeFormat;

    @JsonView(PublicView.class)
    @Schema(description = "The postal code regex", example = "^(\\\\d{4}[A-Z]{2})$")
    String postalCodeRegex;
}
