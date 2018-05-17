# Sprintegration
Sample application for [Tinkoff Java Meetup](https://meetup.tinkoff.ru/events/java-meetup) (May 17, 2018) to 
showcase some basic concepts of 
[Spring Integration](https://projects.spring.io/spring-integration/) framework and its [Java DSL](https://docs.spring.io/spring-integration/docs/5.0.5.RELEASE/reference/html/java-dsl.html).

## Brief Description
The application is built upon Spring Boot and Spring Integration frameworks.
As a test business case it performs the following actions:
- queries for new posts in [Spring Engineering blog](http://spring.io/blog/category/engineering) (Atom feed) by 
schedule and 
then, for every fetched blog post:
- extracts the most mentioned Spring project name (e.g. Boot, Cloud, Framework);
- [searches Twitter](https://twitter.com/search-home) for tweets about extracted Spring project for last week;
- converts every tweet into simple (reduced) model;
- for original (non-retweeted) tweets:
    - saves them to embedded (in-memory) [H2 database](http://www.h2database.com);
- for retweets:
    - converts tweet reduced model into JSON;
    - chooses a filename basing on extracted Spring project name (e.g. boot.txt, cloud.txt);
    - saves retweets to the chosen file in `work/retweets` directory in appending mode.
    
## Tips & Tricks
* H2 web console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
* There are 5 commits in repository that correspond to main steps described above. The application is fully runnable 
on every one of them. Each commit is [tagged](https://github.com/Toparvion/sprintegration/tags) with `Step_x` tag, where `x` is from 1 to 5. 
 
## Build & Run

### System Requirements
- JDK 9 or higher
- Git

First, [clone](https://help.github.com/articles/which-remote-url-should-i-use/#cloning-with-https-urls-recommended) the repository. Then navigate to the cloned repository dir and execute:
##### To build
```
gradlew build
```
##### To run
```
gradlew bootRun
```
or (after successful build)
```
java -jar build\libs\sprintegration-0.0.1-SNAPSHOT.jar
```

