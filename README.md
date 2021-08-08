# Kaitai Struct: runtime library for Java

[![Maven Central](https://img.shields.io/maven-central/v/io.kaitai/kaitai-struct-runtime)](https://search.maven.org/artifact/io.kaitai/kaitai-struct-runtime)

This library implements Kaitai Struct API for Java.

Kaitai Struct is a declarative language used for describe various binary
data structures, laid out in files or in memory: i.e. binary file
formats, network stream packet formats, etc.

Further reading:

* [About Kaitai Struct](http://kaitai.io/)
* [About API implemented in this library](http://doc.kaitai.io/stream_api.html)
* [Java-specific notes](http://doc.kaitai.io/lang_java.html)

# Build
To build library run the following command:

```console
mvn install
```

# Release
To make a release ensure that you have:

- a [gpg](https://gnupg.org/) installed
- a [configured](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html) gpg signing key
- pass `-DperformRelease=true` argument to the maven command invocation:

  ```console
  mvn -DperformRelease=true deploy
  ```

See also http://doc.kaitai.io/developers.html#java.
