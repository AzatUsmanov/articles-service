
val koinVersion = "4.0.0"
val koinAnnotationsVersion = "1.3.1"
val ktorVersion = "3.1.3"

plugins {
	val kotlinVersion = "2.1.20"
	val kspVersion = "2.1.20-1.0.32"  
	val jooqVersion = "9.0"
	val ktorVersion = "3.1.3"

	kotlin("jvm") version kotlinVersion
	id("io.ktor.plugin") version ktorVersion
	id("com.google.devtools.ksp") version kspVersion
	id("nu.studer.jooq") version jooqVersion
	id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
	application
}

group = "pet"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	//koin
	implementation("io.insert-koin:koin-ktor:$koinVersion")
	ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVersion")
	implementation("io.insert-koin:koin-core-jvm:$koinVersion")
	implementation("io.insert-koin:koin-annotations:$koinAnnotationsVersion")
	testImplementation("io.insert-koin:koin-test:$koinVersion")
	testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

	//ktor
	implementation("io.ktor:ktor-server-double-receive:$ktorVersion")
	implementation("io.ktor:ktor-server-auth:$ktorVersion")
	implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-request-validation:$ktorVersion")
	implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
	implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-netty:$ktorVersion")
	implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
	testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
	testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")

	//db
	implementation("org.jooq:jooq:3.19.18")
	implementation("org.flywaydb:flyway-core:11.8.2")
	implementation("com.zaxxer:HikariCP:6.3.0")
	implementation("org.postgresql:postgresql:42.7.5")
	runtimeOnly("org.flywaydb:flyway-database-postgresql:11.8.2")
	jooqGenerator("org.postgresql:postgresql:42.7.5")
	jooqGenerator("com.h2database:h2:2.3.232")
	testImplementation("com.h2database:h2:2.3.232")

	//test
	testImplementation("net.datafaker:datafaker:2.4.3")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("com.jayway.jsonpath:json-path:2.9.0")

	//other
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("ch.qos.logback:logback-classic:1.5.18")
	implementation("org.mindrot:jbcrypt:0.4")
}


ksp {
	arg("KOIN_CONFIG_CHECK", "true")
}

application {
	mainClass.set("io.ktor.server.netty.EngineMain")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jooq {
	version.set("3.19.18")
	edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

	configurations {
		create("main") {
			generateSchemaSourceOnCompilation.set(true)

			jooqConfiguration.apply {
				logging = org.jooq.meta.jaxb.Logging.INFO

				jdbc.apply {
					driver = "org.postgresql.Driver"
					url = "jdbc:postgresql://localhost:5432/articles-kotlin-db"
					user = "postgres"
					password = "postgres"
				}
				generator.apply {

					generate.apply {
						isKotlinNotNullRecordAttributes = true
					}

					name = "org.jooq.codegen.KotlinGenerator"
					database.apply {
						name = "org.jooq.meta.postgres.PostgresDatabase"
						inputSchema = "public"
						includes = ".*"
					}
					generate.apply {
						isTables = true
						isDeprecated = false
						isRecords = true
					}
					target.apply {
						packageName = "pet.articles.generated.jooq"
						directory = "build/generated-src/jooq/main"
					}
					generator.name= "org.jooq.codegen.KotlinGenerator"
				}
			}
		}
	}
}
