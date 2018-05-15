# Sprintegration
Sample application for [Tinkoff Java Meetup](https://meetup.tinkoff.ru/events/java-meetup) (17th of May'18) to 
showcase some basic concepts of 
[Spring Integration](https://docs.spring.io/spring-integration/docs/5.0.4.RELEASE/reference/htmlsingle) framework.

## Brief description
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
    
## Tips & tricks
* H2 web console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
* There are 5 commits in repository that correspond to main steps described above. The application is fully runnable 
on every one of them. Each commit is tagged with `Step_x` tag, where `x` is from 1 to 5. 
 
## Build & run
Navigate to repository dir. Then:
##### To build
```
gradlew build
```
##### To run
```
gradlew bootRun
```
or (after successful run)
```
java -jar build\libs\sprintegration-0.0.1-SNAPSHOT.jar
```

