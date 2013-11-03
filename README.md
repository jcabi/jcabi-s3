<img src="http://img.jcabi.com/logo.png" width="200px" height="48px" />

More details are here: [www.jcabi.com/jcabi-s3](http://www.jcabi.com/jcabi-s3/index.html)

It's an object layer on top of Amazon S3 SDK:

```java
import com.jcabi.s3.Region;
public class Main {
  public static void main(String[] args) {
    Region region = new Region.Simple("key", "secret");
    Bucket bucket = region.bucket("my.example.com");
    Ocket ocket = bucket.ocket("test.txt");
    String content = new Ocket.Plain(ocket).read();
    new Ocket.Plain(ocket).write("hello, world!");
  }
}
```

You need just this dependency:

```xml
<dependency>
  <groupId>com.jcabi</groupId>
  <artifactId>jcabi-s3</artifactId>
  <version>0.1</version>
</dependency>
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/yegor256/jcabi/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
