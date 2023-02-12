package anton.maliar.first.bot.repository;

import anton.maliar.first.bot.repository.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE chat_id = :chatId", nativeQuery = true)
    User getUser(@Param("chatId") Long chatId);
}
