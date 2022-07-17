clean:
	./gradlew clean

check-updates:
	./gradlew dependencyUpdates

lint:
	./gradlew checkstyleMain

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

build: lint test
	./gradlew clean build

.PHONY: build