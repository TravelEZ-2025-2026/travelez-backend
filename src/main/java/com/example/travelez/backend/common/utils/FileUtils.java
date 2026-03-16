package com.example.travelez.backend.common.utils;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileUtils {
    private static final Tika TIKA = new Tika();

    public static boolean isImage(MultipartFile file) {
        try {
            String mimeType = TIKA.detect(file.getInputStream());
            return mimeType.startsWith("image/");
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isVideo(MultipartFile file) {
        try {
            String mimeType = TIKA.detect(file.getInputStream());
            return mimeType.startsWith("video/");
        } catch (IOException e) {
            return false;
        }
    }

    public static String getRealMimeType(MultipartFile file) {
        try {
            return TIKA.detect(file.getInputStream());
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
