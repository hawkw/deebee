scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

name := "deebee"

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
	"com.typesafe.scala-logging" 	%%	"scala-logging-slf4j"   		% "2.1.2",
	"org.scala-lang.modules" 			%%	"scala-parser-combinators"	% "1.0.2",
	"org.scalatest" 							%		"scalatest_2.11" 						% "2.2.1" 	% "test"
)

ScoverageKeys.minimumCoverage := 70

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.highlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else false
}

publishArtifact in Test := false

parallelExecution in Test := false
