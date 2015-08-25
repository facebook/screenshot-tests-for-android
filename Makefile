all:
	.

py-test:
	cd ./src/py/ && python -m unittest discover

# this is a temporary work around while the source of truth is
# internal at Facebook. We'll swap the source of truth later.
oss-sync:
	find . -name *.py -o -name *.java -o -name Makefile -o -name *.sh -o -name *.xml -o -name *.png -o -name *.jar | xargs -i cp -v --parents {} ~/builds/screenshot-test-for-android/
