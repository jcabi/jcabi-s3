<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-s3)](http://www.rultor.com/p/jcabi/jcabi-s3)

[![Build Status](https://travis-ci.org/jcabi/jcabi-s3.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-s3)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-s3)](http://www.0pdd.com/p?name=jcabi/jcabi-s3)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-s3)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.jcabi/jcabi-s3/badge.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-s3)
[![Dependencies](https://www.versioneye.com/user/projects/561aa1eca193340f32000fd3/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561aa1eca193340f32000fd3)
[![Coverage Status](https://coveralls.io/repos/jcabi/jcabi-s3/badge.svg?branch=master&service=github)](https://coveralls.io/github/jcabi/jcabi-s3?branch=master)

More details are here: [s3.jcabi.com](http://s3.jcabi.com/index.html).
Also, read this blog post: [Object-Oriented Java Adapter of Amazon S3 SDK](http://www.yegor256.com/2014/05/26/amazon-s3-java-oop-adapter.html).

It's an object layer on top of Amazon S3 SDK:

```java
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import com.jcabi.s3.cached.Region;
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
