package anton.maliar.first.bot.repository.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "chat_id")
    private Long chatId;
    @OneToOne
    @JoinColumn(name = "location")
    private Location location;
}
