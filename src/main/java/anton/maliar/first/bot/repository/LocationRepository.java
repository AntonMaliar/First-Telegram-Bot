package anton.maliar.first.bot.repository;

import anton.maliar.first.bot.repository.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query(value = "SELECT * FROM locations WHERE latitude = :latitude AND longitude = :longitude",
            nativeQuery = true)
    Location checkLocation(
            @Param("latitude") String latitude,
            @Param("longitude") String longitude
    );
}