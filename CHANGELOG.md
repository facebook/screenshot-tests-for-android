0.15.0 (Feb 2 2022)
------
- Python 3 support
- Added option to specify pulling a `tar` bundle instead of individual files via `bundleResults`. This is useful for reducing the time it takes to pull a large amount of screenshots. It will also help if you experience ADB connection flakiness.

0.14.0 (Apr 22 2021)
------
- Added test orchestrator support
- Switched away from xml to json for metadata files
- Support for Gradle 7

0.13.0 (Jul 8 2020)
------
- Made accessibility node information optional
- Bugfixes surrounding obtaining accessibility node information

0.12.0 (Mar 4 2020)
------
- Added the ability to generate a diff between the old version of a screenshot and the new output in the html report. Currently only works with a configured test image API
- Added support for AGP 3.6 + Gradle 6.2.x

0.11.0 (Oct 17 2019)
------
- Replaced direct usages of deprecated Gradle APIs
- Adopted the Contributor Covenant
- Added `failureDir` which saves the expected, actual and diff images of each failing test when verification fails.

0.10.0 (Jun 11 2019)
------
- Added batch downloading of screenshot images instead of pulling individual files
- Added Accessibility hierarchy information
- Added ability to specify max sizes for images
- Fixed addDeps functionality parameter in the plugin
- Fixed referenceDir functionality in the plugin
- Fixed an issue on Samsung devices where a crash would occur when faking a WindowAttachment
- Migrated to AndroidX

0.9.0 (Apr 1 2019)
-----
- Added a setMaxPixels method to the record builder interface to allow for really large images
- Added an integration point to allow you to see a version of the given screenshot from a server provided service
- Fixed an issue where onGlobalLayoutListener wasn'nt being triggered properly
- Fixed an issue where a parcel file descriptor wasn't being closed
- Added the ability to dump the accessibility hierarchy
- Min SDK has been bumped to 14
- Added the ability to run tests on all connected targets

0.8.0 (Jul 30 2018)
-----
- Replaced androidTestApi with androidTestImplementation when adding in core dependency via the plugin
- Fixed a bug where requesting focus prior to being attached to a Window would crash
- Added the ability to customize the max pixel size restriction
- Moved generated report to build/ instead of /tmp
- Added language to the device name calculation for multiple devices

0.7.0 (Apr 19 2018)
-----
- Added the ability to retrieve the resulting Bitmap for custom use on your RecordBuilder
- Removed the runtime dependency on Dexmaker, this will resolve any issues of using frameworks such as Mockito in your screenshot tests
- Added a a check to fail when resultant screenshots are extremely large
- Rewrote the client plugin to provide screenshot test tasks per applicable variant
- Re-license to Apache 2

0.6.0 (Feb 06 2018)
-----
- Added the ability to run screenshot tests on multiple devices at once
  - Set `multipleDevices` to `true` in your `screenshots` config in your Gradle file to enable this.
- The core module no longer depends on junit
- Upgraded to Gradle 4.4.1
- Removed R and BuildConfig classes from release artifacts
- Added a Buck file for the Python module

0.5.0 (Nov 20 2017)
-----
- Upgraded to AGP 3
- Upgraded to Gradle 4.3
- Added layout-hierarchy-litho module for Litho support in LayoutHierarchy dumps
- Rewrote the entire Layout Hierarchy dump system
- Added more TextView information in hierarchy output
- Added a param for custom Python executables
- Fixed WindowAttachmentTest for API 26+
- Implemented a view hierarchy overlay for screenshots
- Changed the dump output to use JSON instead of XML
- Spruced up the results page (#117)
- Added a dark background toggle button (#116)

0.4.3 (Jul 13 2017)
-----
- Added more examples
- Fixed a longstanding issue where we showed a horizontal break in the screenshots every 512 pixels.

0.4.2 (Sep 28 2016)
-----
- Support for Android gradle plugin 2.2.0
- Make ViewHierarchy dump more useful information
