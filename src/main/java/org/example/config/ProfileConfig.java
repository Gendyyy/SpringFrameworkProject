package org.example.config;

import org.example.config.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class ProfileConfig {
    private static Properties prop;
    private static InputStream input ;

    public ProfileConfig() throws IOException {
        prop = new Properties();
        input = ProfileConfig.class.getClassLoader().getResourceAsStream("config.properties");

    }

    public static String getProfile() throws IOException {
        prop.load(input);
        return prop.getProperty("application.profile.active");
    }
}
