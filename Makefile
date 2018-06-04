TMPFILE:=$(shell mktemp)

.PHONY:
	@true

release-tests: integration-tests
	./gradlew :releaseTests

integration-tests: | install-local sample-tests
	@true

sample-tests:
	./gradlew :sample:screenshotTests 2>&1 | tee $(TMPFILE)

	grep "Found 11 screenshots" $(TMPFILE)

prod-integration-tests: env-check
	cd examples/app-example && ./gradlew connectedAndroidTest
	cd examples/app-example && ./gradlew screenshotTests
