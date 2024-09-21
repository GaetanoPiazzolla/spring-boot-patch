plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("io.freefair.lombok") version "8.6"
	id("com.diffplug.spotless") version "6.25.0"
	id("org.openapi.generator") version "7.4.0"
}

group = "gae.piaz"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(22)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.flipkart.zjsonpatch:zjsonpatch:0.4.16")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

spotless {
	java {
		toggleOffOn()
		googleJavaFormat().aosp()
	}
}

openApiValidate {
	inputSpec.set("$rootDir/api/api-docs.yaml")
}

openApiGenerate {
	generatorName.set("typescript-axios")
	inputSpec.set("$rootDir/api/api-docs.yaml")
	outputDir.set("$rootDir/frontend/api/")
}
