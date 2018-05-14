package tech.toparvion.sample.sprintegration.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.social.twitter.api.HashTagEntity;
import org.springframework.social.twitter.api.Tweet;

import java.util.Date;

import static java.util.stream.Collectors.joining;

/**
 * Simplified version of Twitter {@link Tweet} object for saving to local storage
 *
 * @author Toparvion
 * @since v0.0.1
 */
@Getter
@ToString
public class DemoTweet {
  private final String id;
  private final String author;
  private final Date created;
  private final String tags;
  private final String text;
  private final boolean isRetweet;

  public DemoTweet(Tweet source) {
    id = source.getIdStr();
    author = source.getUser().getName();
    created = source.getCreatedAt();
    tags = !source.getEntities().hasTags()
        ? ""
        : source.getEntities()
                .getHashTags()
                .stream()
                .map(HashTagEntity::getText)
                .collect(joining(" "));
    text = source.getUnmodifiedText();
    isRetweet = source.isRetweet();
  }
}
