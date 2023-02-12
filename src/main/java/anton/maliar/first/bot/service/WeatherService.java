package anton.maliar.first.bot.service;

import anton.maliar.first.bot.repository.LocationRepository;
import anton.maliar.first.bot.repository.UserRepository;
import anton.maliar.first.bot.repository.model.CurrentWeather;
import anton.maliar.first.bot.repository.model.Location;
import anton.maliar.first.bot.repository.model.Session;
import anton.maliar.first.bot.repository.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.*;
import java.util.List;

@Component
@PropertySource("application.properties")
public class WeatherService {
    @Value("${api.key}")
    private String apiKey;
    private UserRepository userRepository;
    private LocationRepository locationRepository;
    private ObjectMapper objectMapper;
    private SessionManager sessionManager;
    private RestTemplate restTemplate;

    @Autowired
    public WeatherService(UserRepository userRepository, LocationRepository locationRepository,
                          ObjectMapper objectMapper, SessionManager sessionManager){
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.restTemplate = new RestTemplate();
    }


    public String weatherCommand(Long chatId) {
        User user = userRepository.getUser(chatId);
        Session session = sessionManager.getSession(chatId);

        //1. if user don't exist in DB
        if(user == null){
            session.setCurrentChannel("/set-location");
            return "Please input your location, first letter must be uppercase:";
        }
        //if user exists in DB - return weather
        return getWeather(chatId, user.getLocation());
    }

    public String setLocation(String location, Long chatId){
        List<Location> listOfLocation = getLocationFromResponse(location);
        User user = userRepository.getUser(chatId);

        if(listOfLocation == null || listOfLocation.size() == 0){
            return "This location was not found. Try again.";
        }

        //if location one
        if(listOfLocation.size() == 1){

            //actions when user doesn't exist in DB
            if(user == null){
                locationRepository.save(listOfLocation.get(0));
                user = new User(chatId, listOfLocation.get(0));
                userRepository.save(user);
                return getWeather(chatId, user.getLocation());
            }

            //if User exists we understand that user want to change location
            Location currentLocation = user.getLocation();
            locationRepository.save(listOfLocation.get(0));
            user.setLocation(listOfLocation.get(0));
            userRepository.save(user);
            locationRepository.delete(currentLocation);

            return getWeather(chatId, user.getLocation());
        }

        //if locations are more than one, ask user to choose correct
        Session session = sessionManager.getSession(chatId);
        session.setListOfLocation(listOfLocation);
        session.setCurrentChannel("/choose-location");

        StringBuilder answer = new StringBuilder("Choose your location by input a number:");
        int i = 0;
        for(Location currentLocation : listOfLocation){
            answer
                .append("\n")
                .append(i + ". ")
                .append(currentLocation.getName()+" "+currentLocation.getState()+" "+currentLocation.getCountry())
                .append("\n")
                .append("https://www.google.com/search?q="+currentLocation.getLatitude()+","+currentLocation.getLongitude())
                .append("\n");
            i++;
        }

        return answer.toString();
    }

    public String setLocationFromList(Long chatId, String numberOfLocation) {
        int numberOfLoc = Integer.parseInt(numberOfLocation);
        User user = userRepository.getUser(chatId);
        Session session = sessionManager.getSession(chatId);
        Location selectedLocation = session.getListOfLocation().get(numberOfLoc);

        //actions when user doesn't exist in DB
        if(user == null){
            locationRepository.save(selectedLocation);
            user = new User(chatId, selectedLocation);
            userRepository.save(user);
            return getWeather(chatId, user.getLocation());
        }

        //if User exists we understand that user want to change location
        Location currentLocation = user.getLocation();
        locationRepository.save(selectedLocation);
        user.setLocation(selectedLocation);
        userRepository.save(user);
        locationRepository.delete(currentLocation);

        return getWeather(chatId, user.getLocation());
    }

    public List<Location> getLocationFromResponse(String location){
        ResponseEntity<String> response = getLocationHttpRequest(location);
        try {
            List<Location> listOfLocation = objectMapper.readValue(response.getBody(), new TypeReference<>(){});
            return listOfLocation;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getWeatherFromResponse(Location location){
        ResponseEntity<String> response = getWeatherHttpRequest(location.getLatitude(), location.getLongitude());
        try {
            CurrentWeather currentWeather = objectMapper.readValue(response.getBody(), CurrentWeather.class);
            return weatherFormat(location, currentWeather);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResponseEntity<String> getLocationHttpRequest(String location) {
        URI uri = URI.create("https://api.api-ninjas.com/v1/geocoding?city="+location);

        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header("Accept", "application/json")
                .header("X-Api-Key", apiKey)
                .build();

        return restTemplate.exchange(requestEntity, String.class);
    }

    private ResponseEntity<String> getWeatherHttpRequest(String latitude, String longitude){
        URI uri = URI.create("https://api.api-ninjas.com/v1/weather?lat="+latitude+"&"+"lon="+longitude);

        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header("Accept", "application/json")
                .header("X-Api-Key", apiKey)
                .build();

        return restTemplate.exchange(requestEntity, String.class);
    }

    private String weatherFormat(Location location, CurrentWeather currentWeather){
        StringBuilder answer = new StringBuilder("Current weather in: ");

        answer.append(location.getName()).append(" ").append(location.getState()).append(" ").append(location.getCountry()).append("\n")
                .append("current temperature: ").append(currentWeather.getTemp()).append("\n")
                .append("wind speed: ").append(currentWeather.getWind_speed()).append("\n")
                .append("humidity: ").append(currentWeather.getHumidity()).append("\n")
                .append("percentage of cloud cover: ").append(currentWeather.getCloud_pct());

        return answer.toString();
    }

    private String getWeather(Long chatId, Location location) {
        sessionManager.deleteSession(chatId);
        return getWeatherFromResponse(location);
    }

}
