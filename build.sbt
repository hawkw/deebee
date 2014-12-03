scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

name := "deebee"

version := "0.1"

scalaVersion := "2.11.4"

resolvers ++= Seq(
	Resolver.sonatypeRepo("releases"),
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.chuusai" 								%%	"shapeless" 									% "2.0.0",
	"com.typesafe.scala-logging" 	%%	"scala-logging-slf4j"   			% "2.1.2",
	"com.typesafe.akka"          	%%	"akka-actor"             			% "2.3.2",
	"com.typesafe.akka"          	%%	"akka-actor-tests"       			% "2.3.2",
	"org.scala-lang.modules" 			%%	"scala-parser-combinators"		% "1.0.2",
	"org.scalatest" 							%		"scalatest_2.11" 							% "2.2.1" % "test",
	"org.scalamock" 							%%	"scalamock-scalatest-support"	% "3.2" 	% "test"
)

ScoverageKeys.minimumCoverage := 70

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.scoverageExcludedFiles := "Demo*.*"

ScoverageKeys.excludedPackages := "<empty>"

ScoverageKeys.highlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else false
}

publishArtifact in Test := false

parallelExecution in Test := false

assemblyJarName in assembly := "deebee-demo.jar"
mainClass in assembly := Some("deebee.DemoShell")
