TMPFILE:=$(shell mktemp)

sample-tests:
	./gradlew :sample:screenshotTests 2>&1 | tee $(TMPFILE)

	grep "Found 11 screenshots" $(TMPFILE)
