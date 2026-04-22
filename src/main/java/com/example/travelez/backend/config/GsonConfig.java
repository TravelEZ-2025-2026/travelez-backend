package com.example.travelez.backend.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                // Cấu hình lưu/đọc LocalDate => "yyyy-MM-dd"
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                })
                // Cấu hình lưu/đọc LocalDateTime => "yyyy-MM-dd'T'HH:mm:ss"
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime dateTime, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                })
                // Cấu hình lưu/đọc LocalTime => "HH:mm:ss"
                .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
                    @Override
                    public JsonElement serialize(LocalTime time, Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(time.format(DateTimeFormatter.ISO_LOCAL_TIME));
                    }
                })
                .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
                    @Override
                    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
                    }
                })
                .create();
    }
}