package com.example.gassistance;

import android.os.Environment;
import java.io.File;

public class FileLoadUtils {
    public static File createFile(String fileName) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, fileName);
    }
}

