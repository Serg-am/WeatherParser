package com.example.weatherparser.controller;

import com.example.weatherparser.service.OpenWeatherMapJsonParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

@Controller
@Slf4j
public class FileController {
    private final OpenWeatherMapJsonParser openWeatherMapJsonParser;

    public FileController(OpenWeatherMapJsonParser openWeatherMapJsonParser) {
        this.openWeatherMapJsonParser = openWeatherMapJsonParser;
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
