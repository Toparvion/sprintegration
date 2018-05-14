package tech.toparvion.sample.sprintegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

@SpringBootApplication
@Slf4j
public class SprintegrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(SprintegrationApplication.class, args);
  }

  /**
   * Подчищает директорию work/retweets при старте приложения, чтобы каждый эксперимент был чистым
   */
  @EventListener(ApplicationStartedEvent.class)
  public void cleanRetweetDir() throws IOException {
    Files.walkFileTree(Paths.get("work/retweets"), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        log.info("Удалил файлик {}", file);
        return CONTINUE;
      }
    });
  }
}
