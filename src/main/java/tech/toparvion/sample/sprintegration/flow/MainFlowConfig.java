package tech.toparvion.sample.sprintegration.flow;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.jdbc.BeanPropertySqlParameterSourceFactory;
import org.springframework.integration.jdbc.JdbcMessageHandler;
import org.springframework.integration.twitter.outbound.TwitterSearchOutboundGateway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import tech.toparvion.sample.sprintegration.model.DemoTweet;

import java.io.File;
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
import static org.springframework.integration.file.support.FileExistsMode.APPEND;

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
  @Value("${blog.schedule:45}")
  private String schedule;
  @Value("${twitter.search.fetch-max:10}")
  private int maxTweets;
  @Value("${twitter.search.exclusions}")
  private List<String> exclusions;

  @Bean
  public IntegrationFlow mainFlow(TwitterTemplate twitterTemplate,
                                  JdbcMessageHandler dbSaver) {
    return from(Feed.inboundAdapter(springBlogFeedUrl, "blog"),
        c -> c.poller(fixedRate(Long.valueOf(schedule), SECONDS)    // cron("*/30 * * * * ?")
                     .maxMessagesPerPoll(1/*только для демонстрации*/)))
        .log(MainFlowConfig::composeSyndEntryLogString)
        .split("payload.contents")            // то же, что и .transform(entry -> entry.getContents().get(0).getValue())
        .transform(SyndContent::getValue)     // то же, что и .transform("payload.value")
        .transform(this::findMostMentionedProject)
        .enrichHeaders(h -> h.headerExpression("tag", "payload.toLowerCase()"))
        .transform(this::prepareTwitterSearchParams)
        .handle(new TwitterSearchOutboundGateway(twitterTemplate))
        .split()
        .transform(DemoTweet::new)
        .log(MainFlowConfig::composeDemoTweetLogString)
        .route("payload.retweet", spec -> spec
            .subFlowMapping(false, flow -> flow.handle(dbSaver))
            .subFlowMapping(true, flow -> flow
                .transform(Transformers.toJson())
                .handle(Files.outboundAdapter(new File("work/retweets"))
                             .appendNewLine(true)
                             .fileExistsMode(APPEND)
                             .fileNameExpression("headers[tag].concat('.txt')"))))
        .get();
  }

  @Bean
  public TwitterTemplate twitterTemplate(@Value("${twitter.consumer.key}") String twitterConsumerKey,
                                         @Value("${twitter.consumer.secret}") String twitterConsumerSecret) {
    return new TwitterTemplate(twitterConsumerKey, twitterConsumerSecret);
  }

  @Bean
  public JdbcMessageHandler dbSaver(JdbcTemplate jdbcTemplate) {
    String sql = "MERGE INTO TWEET VALUES (:payload.id, :payload.author, :payload.created, :payload.tags, :payload.text)";
    JdbcMessageHandler jdbcMessageHandler = new JdbcMessageHandler(jdbcTemplate, sql);
    jdbcMessageHandler.setSqlParameterSourceFactory(new BeanPropertySqlParameterSourceFactory());
    return jdbcMessageHandler;
  }

  private String findMostMentionedProject(String feedEntryContent) {
    Matcher matcher = SPRING_PROJECT_PATTERN.matcher(feedEntryContent);
    Map<String, Integer> hypeIndex = new HashMap<>();
    while (matcher.find()) {
      String hypeCandidate = matcher.group(1);
      if (!exclusions.contains(hypeCandidate.toLowerCase())) {
        hypeIndex.merge(hypeCandidate, 1, Integer::sum);
      } else {
        log.info("Название '{}' пропущено, так как числится в исключения: {}", hypeCandidate, exclusions);
      }
    }
    log.info("Индекс упоминаемых проектов: {}", hypeIndex);
    String mostHypedTerm = hypeIndex.entrySet()
        .stream()
        .max(comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new IllegalArgumentException("No Spring project mentions found in given feed entry."));
    log.info("Самый упоминаемый проект: {}", mostHypedTerm);
    return mostHypedTerm;
  }

  private SearchParameters prepareTwitterSearchParams(String mostMentionedProject) {
    SearchParameters searchParameters = new SearchParameters("#Spring" + mostMentionedProject)
        .count(maxTweets)
        .lang("en");
    log.info("Запрашиваю последние {} твитов по теме: {}", searchParameters.getCount(), searchParameters.getQuery());
    return searchParameters;
  }

  private static String composeSyndEntryLogString(Message<SyndEntry> message) {
    return format("Новая запись в блоге:\nЗаголовок: %s\nАвтор: %s\nДата: %s", message.getPayload().getTitle(),
        message.getPayload().getAuthor(), message.getPayload().getUpdatedDate());
  }

  private static String composeDemoTweetLogString(Message<DemoTweet> message) {
    DemoTweet tweet = message.getPayload();
    return format("Твит id=%s\nСоздан: %s\nАвтор: %s\nТэги: %s\nРетвит: %s\nПолный текст: %s", tweet.getId(),
        tweet.getCreated(), tweet.getAuthor(), tweet.getTags(), tweet.isRetweet(), tweet.getText());
  }

}