package tech.toparvion.sample.sprintegration.flow;

import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.messaging.Message;

import java.net.URL;

import static java.lang.String.format;
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
public class MainFlowConfig {

  @Value("${blog.url}")
  private URL springBlogFeedUrl;
  @Value("${blog.pollPeriodSec:45}")
  private long pollPeriod;

  @Bean
  public IntegrationFlow mainFlow() {
    return from(Feed.inboundAdapter(springBlogFeedUrl, "blog"),
                conf -> conf.poller(fixedRate(pollPeriod, SECONDS)))            // cron("*/30 * * * * ?")
        .log(MainFlowConfig::composeSyndEntryLogString)
        .get();
  }

  private static String composeSyndEntryLogString(Message<SyndEntry> message) {
    return format("Новая запись в блоге:\n Заголовок: %s\n Автор: %s\n Дата: %s", message.getPayload().getTitle(),
        message.getPayload().getAuthor(), message.getPayload().getUpdatedDate());
  }

}
