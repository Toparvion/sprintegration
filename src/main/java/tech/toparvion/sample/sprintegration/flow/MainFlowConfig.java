package tech.toparvion.sample.sprintegration.flow;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.messaging.Message;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Comparator.comparingInt;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.integration.dsl.IntegrationFlows.from;
import static org.springframework.integration.dsl.Pollers.fixedRate;

/**
 * Основной (и единственный) интгерационный конвейер микросервиса со всеми запчастями
 *
 * @author Toparvion
 * @since v0.0.1
 */
@Configuration
@Slf4j
public class MainFlowConfig {
  private static final Pattern SPRING_PROJECT_PATTERN = Pattern.compile("Spring\\s+([A-Z][a-z]+)");

  @Value("${blog.url}")
  private URL springBlogFeedUrl;
  @Value("${blog.pollPeriodSec:45}")
  private long pollPeriod;
  @Value("${twitter.search.exclusions}")
  private List<String> exclusions;

  @Bean
  public IntegrationFlow mainFlow() {
    return from(Feed.inboundAdapter(springBlogFeedUrl, "blog"),
                conf -> conf.poller(fixedRate(pollPeriod, SECONDS)))            // cron("*/30 * * * * ?")
        .log(MainFlowConfig::composeSyndEntryLogString)
        // .transform(entry -> entry.getContents().get(0).getValue())
        .split("payload.contents")
        .transform(SyndContent::getValue)                                   // то же, что и .transform("payload.value")
        .transform(this::findMostMentionedProject)
        .log(Message::getPayload)
        .get();
  }

  private String findMostMentionedProject(String feedEntryContent) {
    Matcher matcher = SPRING_PROJECT_PATTERN.matcher(feedEntryContent);
    Map<String, Integer> hypeIndex = new HashMap<>();
    // находим все упоминания проектов Spring, обходя исключения
    while (matcher.find()) {
      String hypeCandidate = matcher.group(1);
      if (!exclusions.contains(hypeCandidate.toLowerCase())) {
        hypeIndex.merge(hypeCandidate, 1, Integer::sum);
      } else {
        log.info("Название '{}' пропущено, так как числится в исключениях: {}", hypeCandidate, exclusions);
      }
    }
    log.info("Индекс упоминаемых проектов: {}", hypeIndex);
    // определяем самый часто упоминаемый из них
    return hypeIndex.entrySet()
        .stream()
        .max(comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new IllegalArgumentException("Ни одного упоминания проектов Spring не найдено"));
  }


  private static String composeSyndEntryLogString(Message<SyndEntry> message) {
    return format("Новая запись в блоге:\n Заголовок: %s\n Автор: %s\n Дата: %s", message.getPayload().getTitle(),
        message.getPayload().getAuthor(), message.getPayload().getUpdatedDate());
  }

}
