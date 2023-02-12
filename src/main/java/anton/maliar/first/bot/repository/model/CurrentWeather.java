package anton.maliar.first.bot.repository.model;

import lombok.Data;

@Data
public class CurrentWeather {
    private String wind_speed;
    private String wind_degrees;
    private String temp;
    private String humidity;
    private String sunset;
    private String min_temp;
    private String cloud_pct;
    private String feels_like;
    private String sunrise;
    private String max_temp;
}

