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
  testImplementation(libs.junit)
  testImplementation(libs.assertj)
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

  testLogging.showStandardStreams = true

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
