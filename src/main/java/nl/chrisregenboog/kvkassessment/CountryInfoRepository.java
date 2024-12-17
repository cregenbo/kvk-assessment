package nl.chrisregenboog.kvkassessment;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryInfoRepository extends CrudRepository<CountryInfo, String> {

    Optional<CountryInfo> findByCountryCode(@NonNull String countryCode);

}
