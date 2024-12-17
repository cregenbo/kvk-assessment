package nl.chrisregenboog.kvkassessment;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@OpenAPIDefinition(
		info = @Info(
				title = "Country Info API",
				version = "0.1",
				description = "API for retrieving country information"
		)
)
@RestController("/country")
@RequiredArgsConstructor
public class CountryInfoAPIController {

	static final String REST_COUNTRIES_API_ENDPOINT = "https://restcountries.com/v3.1/alpha/";
	static final WebClient REST_COUNTRIES_WEB_CLIENT = WebClient.create(REST_COUNTRIES_API_ENDPOINT);

	final CountryInfoRepository countryInfoRepository;

	@Operation(
			summary = "Get country information",
			description = "Get information about a country by its country code",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Country information found",
							content = @Content(
									mediaType = MediaType.APPLICATION_JSON_VALUE,
									schema = @Schema(implementation = CountryInfo.class)
							)
					),
					@ApiResponse(
							responseCode = "204",
							description = "Country information is not present in the database",
							content = @Content()
					)
			}
	)
	@GetMapping(
			path = "/{countryCode}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(PublicView.class)
	Mono<ResponseEntity<CountryInfo>> getCountry(@PathVariable @NonNull @CountryCodeParameter String countryCode) {
		// Note that directly invoking blocking repository code in reactive code is normally not recommended
		return Mono.just(countryInfoRepository.findByCountryCode(countryCode)
				.map(ResponseEntity::ok)
				.orElseGet(() ->ResponseEntity.noContent().build()));
	}

	@Operation(
			summary = "Add country information",
			description = "Add information about a country by its country code",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Country information added to the database"
					),
					@ApiResponse(
							responseCode = "500",
							description = "If the country code is invalid or the country information could not be retrieved"
					)
			}
	)

	@PostMapping("/{countryCode}")
	Mono<Void> addCountry(@PathVariable @NonNull @CountryCodeParameter String countryCode) {
		return getRestCountriesAPIResponseMono(countryCode)
				.map(restCountriesAPIResponse -> toCountry(countryCode, restCountriesAPIResponse))
				// Note that directly invoking blocking repository code in reactive code is normally not recommended
				.doOnNext(countryInfoRepository::save)
				.then();
	}

	static Mono<RestCountriesAPIResponse> getRestCountriesAPIResponseMono(@NonNull String countryCode) {
		return REST_COUNTRIES_WEB_CLIENT.get()
				.uri(countryCode)
				.retrieve()
				// Body is a JSON array, but we are only interested in the first element
				.bodyToFlux(RestCountriesAPIResponse.class)
				.next();
	}

	static CountryInfo toCountry(
			@NonNull String countryCode,
			@NonNull RestCountriesAPIResponse restCountriesAPIResponse) {
		return new CountryInfo(
				countryCode,
				restCountriesAPIResponse.name().common(),
				restCountriesAPIResponse.postalCode().format(),
				restCountriesAPIResponse.postalCode().regex());
	}

	record RestCountriesAPIResponse(
			@NonNull Name name,
			@NonNull PostalCode postalCode
	) {
	}

	record Name(
			@NonNull String common
	) {
	}

	record PostalCode(
			@NonNull String format,
			@NonNull String regex
	) {
	}

	@Target({ElementType.PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	@Parameter(description = "The country code", example = "NL")
	@interface CountryCodeParameter {
	}
}
