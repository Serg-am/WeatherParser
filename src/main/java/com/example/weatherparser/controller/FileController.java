package com.example.weatherparser.controller;

import com.example.weatherparser.dto.WeatherDataDTO;
import com.example.weatherparser.service.OpenWeatherMapJsonParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Scanner;

@Controller
@Slf4j
public class FileController {
    private final OpenWeatherMapJsonParser openWeatherMapJsonParser;
    private final WeatherDataDTO weatherDataDTO;

    public FileController(OpenWeatherMapJsonParser openWeatherMapJsonParser, WeatherDataDTO weatherDataDTO) {
        this.openWeatherMapJsonParser = openWeatherMapJsonParser;
        this.weatherDataDTO = weatherDataDTO;
    }

    @PostConstruct
    public void getWeatherForCity() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите город: ");
        String city = scanner.nextLine();
        scanner.close();


        // Вывод данных о погоде в консоль
        System.out.println("City: " + openWeatherMapJsonParser.getReadyForecast(city));
        writeStringToFile(openWeatherMapJsonParser.getReadyForecast(city));

        // Сохраняем данные в бд
        weatherDataDTO.saveCity(city);
        weatherDataDTO.saveWeatherData(weatherDataDTO.getRegionId(city), openWeatherMapJsonParser.getCurrentTemperature(city), openWeatherMapJsonParser.getCurrentWeatherDescription(city), Timestamp.valueOf(LocalDateTime.now()));
        log.info("Произведена запись в базуданных");
    }

    public void writeStringToFile(String content) {
        String filePath = "/Users/sergey/temp/1.txt";

        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.close();
            log.info("Строка успешно записана в файл: " + filePath);
        } catch (IOException e) {
            log.error("Ошибка при записи в файл: " + e.getMessage());
        }
    }

}
