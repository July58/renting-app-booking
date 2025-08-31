package org.example.bookingrent;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BookingRentApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BookingRentApplication.class);
        app.addInitializers(new DotenvInitializer());
        app.run(args);
    }

    static class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(e -> {
                String key = e.getKey();
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, e.getValue());
                }
            });
        }
    }
}

