package com.example.weatherparser.dto;

import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class WeatherDataDTO {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/weather_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "43760097";

    public int getRegionId(String cityName){
        int cityId = -1;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String selectCityQuery = "SELECT id FROM region WHERE city = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectCityQuery)) {
                preparedStatement.setString(1, cityName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        cityId = resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cityId;
    }
    public void saveCity(String cityName) {
        int cityId = getRegionId(cityName);

        if (cityId == -1) {
            // Город не найден, создаем новую запись
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String insertCityQuery = "INSERT INTO region (city) VALUES (?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertCityQuery)) {
                    preparedStatement.setString(1, cityName);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void saveWeatherData(int cityId, double temperature, String description, Timestamp forecastTime) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertWeatherQuery = "INSERT INTO weather_data (region_id, temperature, weather_condition, datetime) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertWeatherQuery)) {
                preparedStatement.setInt(1, cityId);
                preparedStatement.setDouble(2, temperature);
                preparedStatement.setString(3, description);
                preparedStatement.setTimestamp(4, forecastTime);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

