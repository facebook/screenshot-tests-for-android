

.PHONY:
	@true

env-check:
	@echo Checking if emulator or device is connected for tests
	adb get-serialno

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
