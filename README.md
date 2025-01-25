# Object-Oriented S3 Adapter for Java

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C3RUBL5H9.svg)](https://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-s3)](http://www.rultor.com/p/jcabi/jcabi-s3)

[![mvn](https://github.com/jcabi/jcabi-s3/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-s3/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-s3)](http://www.0pdd.com/p?name=jcabi/jcabi-s3)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-s3.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-s3)
[![codecov](https://codecov.io/gh/jcabi/jcabi-s3/branch/master/graph/badge.svg)](https://codecov.io/gh/jcabi/jcabi-s3)

More details are here:
[s3.jcabi.com](http://s3.jcabi.com/index.html).

Also, read this blog post:
[Object-Oriented Java Adapter of Amazon S3 SDK][blog].

It's an object layer on top of Amazon S3 SDK:

```java
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import com.jcabi.s3.cached.CdRegion;
public class Main {
  public static void main(String[] args) {
    Region region = new CdRegion(
      new Region.Simple("key", "secret")
    );
    Bucket bucket = region.bucket("my.example.com");
    Ocket.Text ocket = new Ocket.Text(bucket.ocket("test.txt"));
    String content = ocket.read();
    ocket.write("hello, world!");
  }
}
```

It is highly recommended to use `CdRegion` to avoid multiple duplicate
reads from the same S3 object.

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```bash
mvn clean install -Pqulice
```

[blog]: http://www.yegor256.com/2014/05/26/amazon-s3-java-oop-adapter.html
