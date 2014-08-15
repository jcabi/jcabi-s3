<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-s3)](http://www.rultor.com/p/jcabi/jcabi-s3)

[![Build Status](https://travis-ci.org/jcabi/jcabi-s3.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-s3)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3)

More details are here: [s3.jcabi.com](http://s3.jcabi.com/index.html)

It's an object layer on top of Amazon S3 SDK:

```java
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
public class Main {
  public static void main(String[] args) {
    Region region = new Region.Simple("key", "secret");
    Bucket bucket = region.bucket("my.example.com");
    Ocket.Text ocket = new Ocket.Text(bucket.ocket("test.txt"));
    String content = ocket.read();
    ocket.write("hello, world!");
  }
}
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-s3/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
