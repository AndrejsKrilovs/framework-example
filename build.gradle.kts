plugins {
  id("java")
}

group = "krilovs.andrejs.payment"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

val jettyVersion = "12.1.14"
val jerseyVersion = "4.0.3"
val jacksonVersion = "2.22.4"
val lombokVersion = "1.18.48"
val mapstructVersion = "1.6.3"
val agroalVersion = "3.2.1"

dependencies {
  implementation("org.eclipse.jetty:jetty-server:${jettyVersion}")
  implementation("org.eclipse.jetty.ee11:jetty-ee11-servlet:${jettyVersion}")

  // connection pool with built-in Narayana (JTA) integration
  implementation("io.agroal:agroal-pool:${agroalVersion}")
  implementation("io.agroal:agroal-narayana:${agroalVersion}")
  runtimeOnly("org.postgresql:postgresql:42.7.14")

  // JPA implementation (pulls in jakarta.persistence-api transitively)
  implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.9")
  implementation("org.liquibase:liquibase-core:5.0.4")

  implementation("jakarta.inject:jakarta.inject-api:2.0.1")
  // JTA: API plus the Narayana transaction manager (no CDI: TransactionFeature gives @Transactional its meaning)
  implementation("jakarta.transaction:jakarta.transaction-api:2.0.1")
  implementation("org.jboss.narayana.jta:narayana-jta:7.3.4.Final")
  implementation("org.glassfish.jersey.ext:jersey-bean-validation:${jerseyVersion}")

  //Jakarta REST annotations + hk2 dependency injection implementation
  implementation("org.glassfish.jersey.containers:jersey-container-servlet:${jerseyVersion}")
  implementation("org.glassfish.jersey.inject:jersey-hk2:${jerseyVersion}")
  implementation("org.glassfish.jersey.media:jersey-media-json-jackson:${jerseyVersion}")

  compileOnly("org.projectlombok:lombok:${lombokVersion}")
  annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

  compileOnly("org.mapstruct:mapstruct:${mapstructVersion}")
  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
  annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")

  // Classpath scanning of @Singleton classes in AppBinder
  implementation("io.github.classgraph:classgraph:4.8.184")

  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("ch.qos.logback:logback-classic:1.6.5")
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}