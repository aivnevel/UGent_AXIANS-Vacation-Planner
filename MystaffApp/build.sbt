name := """java-play-angular-seed"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean).settings(
  watchSources ++= (baseDirectory.value / "public/ui" ** "*").get,
  PlayKeys.playDefaultPort := 9000
)

scalaVersion := "2.12.2"

libraryDependencies += guice

// Javax mail
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.6.2"

// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.194"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies ++= Seq(evolutions, jdbc)
// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
// Database
// libraryDependencies += "com.h2database" % "h2" % "1.4.192"
