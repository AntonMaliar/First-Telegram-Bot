package anton.maliar.first.bot.service;

import anton.maliar.first.bot.config.BotConfig;
import anton.maliar.first.bot.repository.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private SessionManager sessionManager;
    private WeatherService weatherService;

    @Autowired
    public TelegramBot(BotConfig botConfig, SessionManager sessionManager, WeatherService weatherService){
        this.botConfig = botConfig;
        this.sessionManager = sessionManager;
        this.weatherService = weatherService;
        initMenu();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    private void initMenu(){
        List<BotCommand> listOfCommands = List.of(
                new BotCommand("/start", "Hi this bot can give you info about weather, use menu in left bottom to interact with bot."),
                new BotCommand("/weather", "Gives info about weather."),
                new BotCommand("/change_location", "Change your current location")
        ).stream().toList();

        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String answer){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        Session session = sessionManager.hasSession(chatId);

        if(session.getCurrentChannel() != null){
            channels(update);
        }else {
            commands(update);
        }
    }

    private void channels(Update update){
        Long chatId = update.getMessage().getChatId();
        String channelName = sessionManager.getSession(chatId).getCurrentChannel();

        switch (channelName){
            case "/set-location":
                String location = update.getMessage().getText();
                sendMessage(chatId, weatherService.setLocation(location, chatId));
                break;
            case "/choose-location":
                String numberOfLocation = update.getMessage().getText();
                sendMessage(chatId, weatherService.setLocationFromList(chatId, numberOfLocation));
                break;
        }
    }

    public void commands(Update update){
        Long chatId = update.getMessage().getChatId();
        String command = update.getMessage().getText();

        switch (command){
            case "/start":
                sendMessage(chatId,"Hi this bot can give you info about weather, use menu in left bottom to interact with bot.");
                break;
            case "/weather":
                sendMessage(chatId, weatherService.weatherCommand(chatId));
                break;
            case "/change_location":
                sessionManager.getSession(chatId).setCurrentChannel("/set-location");
                sendMessage(chatId,"Input your new location:");
                break;
            default:
                sendMessage(chatId,  "No such command");
        }
    }

}



















