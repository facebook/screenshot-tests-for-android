# screenshot-test-for-android

screenshot-test-for-android is a library that can generate fast
deterministic screenshots while running instrumentation tests in
android.

We mimic Android's measure(), layout() and draw() to generate screenshots
on the test thread. By not having to do the rendering on a separate
thread we have control over animations and handler callbacks which
makes the screenshots extremely deterministic and reliable for catching
regressions in continuous integration.

We also provide utilities for using screenshot tests during the development
process. With these scripts you can iterate on a view or layout and quickly
see how the view renders in real android code, without having to
build the whole app. You can also render the view in multiple configurations
at one go.

## Examples

We have a presentation coming up in Droidcon NYC 2015, we'll post
a link to our presentation once it's published.

## Requirements
screenshot-test-for-android works with
* Mac OS X or Linux

The host tooling probably doesn't work on Windows, but can be made to
work with a little effort.

## Building screenshot-test-for-android

<pre>
  ./gradlew build
</pre>

This should build the aar file in builds directory. You still need the
pull_screenshots script from the main repository.


## Join the screenshot-test-for-android community

* Website: http://github.com/facebook/screenshot-test-for-android
* Discussion group:
   https://groups.google.com/forum/#!forum/screenshot-test-for-android
   screenshot-test-for-android@googlegroups.com

See the CONTRIBUTING file for how to help out.

## Authors

screenshot-test-for-android has been written by Arnold Noronha (arnold@fb.com)
You can reach him at @tdrhq on GitHub.

## License

screenshot-test-for-android is BSD-licensed. We also provide an
additional patent grant.
