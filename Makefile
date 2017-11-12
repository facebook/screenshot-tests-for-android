

FILES_WITH_VERSIONS = \
	gradle.properties \
	examples/app-example/build.gradle \
	examples/app-example-androidjunitrunner/build.gradle \
	plugin/src/main/groovy/com/facebook/testing/screenshot/build/ScreenshotsPlugin.groovy

OLD_VERSION=$(shell grep VERSION_NAME gradle.properties |  cut -d '=' -f 2)

TMPFILE:=$(shell mktemp)

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

old-release:
	echo $(OLD_VERSION)

cleanup:
	rm -rf ~/.m2/repository/com/facebook/testing/screenshot/
	./gradlew clean

release-tests: integration-tests
	./gradlew :releaseTests

integration-tests: |  env-check cleanup install-local app-example-tests app-example-androidjunitrunner-tests cleanup
	@true

app-example-tests:
	cd examples/app-example && ./gradlew connectedAndroidTest
	cd examples/app-example && ./gradlew screenshotTests 2>&1 | tee $(TMPFILE)

	grep "Found 3 screenshots" $(TMPFILE)

app-example-androidjunitrunner-tests:
	cd examples/app-example-androidjunitrunner && ./gradlew screenshotTests 2>&1 | tee $(TMPFILE)

	grep "Found 3 screenshots" $(TMPFILE)

app-example-litho-tests:
	cd examples/app-example-litho && ./gradlew screenshotTests 2>&1 | tee $(TMPFILE)
	grep "Found 1 screenshots" $(TMPFILE)


install-local:
	./gradlew :plugin:install
	./gradlew :core:install
	./gradlew :layout-hierarchy-common:install
	./gradlew :layout-hierarchy-litho:install

version-tag:
	git tag v$(OLD_VERSION)
	git push origin v$(OLD_VERSION)

prod-integration-tests: env-check
	$(MAKE) cleanup
	cd examples/app-example && ./gradlew connectedAndroidTest
	cd examples/app-example && ./gradlew screenshotTests
