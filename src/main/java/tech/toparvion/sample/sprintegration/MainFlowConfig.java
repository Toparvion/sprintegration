package tech.toparvion.sample.sprintegration;

import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;

import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.integration.dsl.IntegrationFlows.from;
import static org.springframework.integration.dsl.Pollers.fixedDelay;
import static org.springframework.integration.feed.dsl.Feed.inboundAdapter;

/**
 * @author Toparvion
 * @since v0.0.1
 */
@Configuration
public class MainFlowConfig {

  @Bean
  public IntegrationFlow mainFlow() throws MalformedURLException {
    return from(inboundAdapter(new URL("https://spring.io/blog/category/engineering.atom"), "atom"),
        c -> c.poller(fixedDelay(5000)
                     .maxMessagesPerPoll(1)))
        .<SyndEntry, String>
            transform(entry -> String.format("Atom Entry:\ntitle: %s\nauthor: %s\ndate: %s",
            entry.getTitle(), entry.getAuthor(), entry.getUpdatedDate()))
    .log(Message::getPayload)
    .get();
  }
}
