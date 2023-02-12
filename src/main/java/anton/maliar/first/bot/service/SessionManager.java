package anton.maliar.first.bot.service;

import anton.maliar.first.bot.repository.model.Session;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
public class SessionManager {
    private HashMap<Long, Session> sessions;


    public SessionManager(){
        sessions = new HashMap<>();
    }

    public Session hasSession(Long chatId){
        if(sessions.containsKey(chatId)){
            return sessions.get(chatId);
        }
        Session session = new Session();
        sessions.put(chatId, session);
        return session;
    }

    public Session getSession(Long chatId) {
        return sessions.get(chatId);
    }

    public void deleteSession(Long chatId) {
        sessions.remove(chatId);
    }
}
