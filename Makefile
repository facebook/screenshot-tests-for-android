TMPFILE:=$(shell mktemp)

.PHONY:
	@true

cleanup:
	rm -rf ~/.m2/repository/com/facebook/testing/screenshot/
	./gradlew plugin:clean core:clean layout-hierarchy-common:clean layout-hierarchy-litho:clean

release-tests: integration-tests
	./gradlew :releaseTests

integration-tests: |  cleanup install-local sample-tests cleanup
	@true

sample-tests:
	./gradlew :sample:screenshotTests 2>&1 | tee $(TMPFILE)

	grep "Found 11 screenshots" $(TMPFILE)

prod-integration-tests: env-check
	$(MAKE) cleanup
	cd examples/app-example && ./gradlew connectedAndroidTest
	cd examples/app-example && ./gradlew screenshotTests
