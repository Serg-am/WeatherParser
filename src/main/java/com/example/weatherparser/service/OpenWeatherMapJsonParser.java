package com.example.weatherparser.service;


import com.example.weatherparser.config.WeatherConfig;
import com.example.weatherparser.utils.WeatherUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@Configuration
@Slf4j
public class OpenWeatherMapJsonParser {
    public OpenWeatherMapJsonParser(WeatherConfig weatherConfig) {
        this.weatherConfig = weatherConfig;
    }

    private WeatherConfig weatherConfig ;
    private final static DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter OUTPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM-dd HH:mm", Locale.US);


    public String getReadyForecast(String city) {
        String result;
        try {
            String jsonRawData = downloadJsonRawData(city);
            List<String> linesOfForecast = convertRawDataToList(jsonRawData);
            result = String.format("%s:%s%s", city, System.lineSeparator(), parseForecastDataFromList(linesOfForecast));
        } catch (IllegalArgumentException e) {
            return String.format("Не могу найти город \"%s\".", city);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "В данный момент погодный сервис не исправен, приносим свои извинения, в ближайшее время работа возобновится. Хорошего дня!";
        }
        return result;
    }

    private String downloadJsonRawData(String city) throws Exception {
        String urlString = weatherConfig.getApiTemplate() + city + weatherConfig.getApiKey();
        URL urlObject = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", weatherConfig.getUserAgent());

        int responseCode = connection.getResponseCode();
        if (responseCode == 404) {
            throw new IllegalArgumentException();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private static List<String> convertRawDataToList(String data) throws Exception {
        List<String> weatherList = new ArrayList<>();

        JsonNode arrNode = new ObjectMapper().readTree(data).get("list");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                String forecastTime = objNode.get("dt_txt").toString();
                if (forecastTime.contains("12:00")) {
                    weatherList.add(objNode.toString());
                }
            }
        }
        return weatherList;
    }

    private static String parseForecastDataFromList(List<String> weatherList) throws Exception {
        final StringBuffer sb = new StringBuffer();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String line : weatherList) {
            {
                String dateTime;
                JsonNode mainNode;
                JsonNode weatherArrNode;
                try {
                    mainNode = objectMapper.readTree(line).get("main");
                    weatherArrNode = objectMapper.readTree(line).get("weather");
                    for (final JsonNode objNode : weatherArrNode) {
                        dateTime = objectMapper.readTree(line).get("dt_txt").toString();
                        sb.append(formatForecastData(dateTime, objNode.get("main").toString(), mainNode.get("temp").asDouble()));
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return sb.toString();
    }

    private static String formatForecastData(String dateTime, String description, double temperature) throws Exception {
        LocalDateTime forecastDateTime = LocalDateTime.parse(dateTime.replaceAll("\"", ""), INPUT_DATE_TIME_FORMAT);
        String formattedDateTime = forecastDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        String formattedTemperature;
        long roundedTemperature = Math.round(temperature);
        if (roundedTemperature > 0) {
            formattedTemperature = "+" + Math.round(temperature);
        } else {
            formattedTemperature = String.valueOf(Math.round(temperature));
        }

        String formattedDescription = description.replaceAll("\"", "");

        String weatherIconCode = WeatherUtils.weatherIconsCodes.get(formattedDescription);

        return String.format("%s   %s %s %s%s", formattedDateTime, formattedTemperature, formattedDescription, weatherIconCode, System.lineSeparator());
    }

    public double getCurrentTemperature(String city) {
        try {
            String jsonRawData = downloadJsonRawData(city);
            return parseCurrentTemperature(jsonRawData);
        } catch (IllegalArgumentException e) {
            log.error("Не могу найти город \"{}\".", city);
            throw new IllegalArgumentException("Город не найден");
        } catch (Exception e) {
            log.error("Ошибка при получении данных о погоде: {}", e.getMessage());
            throw new RuntimeException("Ошибка при получении данных о погоде");
        }
    }

    private double parseCurrentTemperature(String data) throws Exception {
        JsonNode currentWeatherNode = new ObjectMapper().readTree(data).get("list").get(0);

        if (currentWeatherNode != null) {
            return currentWeatherNode.get("main").get("temp").asDouble();
        } else {
            log.error("Данные о погоде на текущий момент не найдены.");
            throw new RuntimeException("Данные о погоде на текущий момент не найдены");
        }
    }

    public String getCurrentWeatherDescription(String city) {
        try {
            String jsonRawData = downloadJsonRawData(city);
            return parseCurrentWeatherDescription(jsonRawData);
        } catch (IllegalArgumentException e) {
            log.error("Не могу найти город \"{}\".", city);
            throw new IllegalArgumentException("Город не найден");
        } catch (Exception e) {
            log.error("Ошибка при получении данных о погоде: {}", e.getMessage());
            throw new RuntimeException("Ошибка при получении данных о погоде");
        }
    }

    private String parseCurrentWeatherDescription(String data) throws Exception {
        JsonNode currentWeatherNode = new ObjectMapper().readTree(data).get("list").get(0);

        if (currentWeatherNode != null) {
            JsonNode weather = currentWeatherNode.get("weather").get(0);
            return weather.get("main").asText();
        } else {
            log.error("Данные о погоде на текущий момент не найдены.");
            throw new RuntimeException("Данные о погоде на текущий момент не найдены");
        }
    }

}

