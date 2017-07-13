

FILES_WITH_VERSIONS = \
	gradle.properties \
	examples/app-example/build.gradle \
	examples/app-example-androidjunitrunner/build.gradle \
	plugin/src/main/groovy/com/facebook/testing/screenshot/build/ScreenshotsPlugin.groovy

OLD_VERSION=$(shell grep VERSION_NAME gradle.properties |  cut -d '=' -f 2)

.PHONY:
	@true

env-check:
	@echo Checking if emulator or device is connected for tests
	adb get-serialno

set-release:
	[ x$(NEW_VERSION) != x ]
	[ x$(OLD_VERSION) != x ]
	for file in $(FILES_WITH_VERSIONS) ; do \
		test -f $$file ; \
	done
	for file in $(FILES_WITH_VERSIONS) ; do \
		sed -i 's/$(OLD_VERSION)/$(NEW_VERSION)/' $$file ; \
	done

cleanup:
	rm -rf ~/.m2 ~/.gradle/caches
	rm -rf */build/
	rm -rf examples/one/build/

integration-tests: env-check
	$(MAKE) cleanup
	./gradlew :plugin:install
	./gradlew :core:install

	cd examples/app-example && ./gradlew connectedAndroidTest
	cd examples/app-example && ./gradlew screenshotTests

	$(MAKE) cleanup

version-tag:
	git tag v$(OLD_VERSION)
	git push origin v$(OLD_VERSION)
