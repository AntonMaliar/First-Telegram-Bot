package anton.maliar.first.bot.repository.model;

import jakarta.persistence.Transient;
import lombok.Data;

import java.util.List;

@Data
public class Session {
    private String currentChannel;
    private List<Location> listOfLocation;
}
