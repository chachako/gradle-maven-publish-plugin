plugins {
  id("shared")
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
  compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
  runtimeClasspath += output + compileClasspath
}
val integrationTestImplementation = configurations["integrationTestImplementation"]
  .extendsFrom(configurations.testImplementation.get())

dependencies {
  api(gradleApi())
  api(libs.kotlin.stdlib)

  compileOnly(libs.dokka)
  compileOnly(libs.kotlin.plugin)
  compileOnly(libs.android.plugin)

  implementation(projects.nexus)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.testParameterInjector)
  testImplementation(libs.truth)
  testImplementation(libs.truth.java8)
  testImplementation(libs.truth.testKit)

  // TODO remove after all old integration tests were migrated
  testCompileOnly(libs.junit)
  testRuntimeOnly(libs.junit.vintage)
  testImplementation(libs.assertj)
  testImplementation(libs.maven.model)
}

val integrationTest by tasks.registering(Test::class) {
  dependsOn(
    tasks.publishToMavenLocal,
    projects.nexus.dependencyProject.tasks.publishToMavenLocal
  )
  mustRunAfter(tasks.test)

  description = "Runs the integration tests."
  group = "verification"

  testClassesDirs = integrationTestSourceSet.output.classesDirs
  classpath = integrationTestSourceSet.runtimeClasspath

  useJUnitPlatform()
  testLogging.showStandardStreams = true
  maxHeapSize = "1g"
  jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")

  systemProperty("com.vanniktech.publish.version", version.toString())

  beforeTest(
    closureOf<TestDescriptor> {
      logger.lifecycle("Running test: $this")
    }
  )
}

tasks.check {
  dependsOn(integrationTest)
}
