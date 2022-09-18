clean:
	./gradlew clean

check-updates:
	./gradlew dependencyUpdates

install:
	./gradlew install

generate-migrations:
	./gradlew generateMigrations

start:
	APP_ENV=development ./gradlew run

start-dist:
	APP_ENV=production ./build/install/app/bin/app

lint:
	./gradlew checkstyleMain

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

build:
	./gradlew clean build

.PHONY: build