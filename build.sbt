import com.typesafe.sbt.SbtGit._
import sbtassembly.AssemblyKeys

versionWithGit

git.baseVersion := "0.1"

scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

name := "deebee"

scalaVersion := "2.11.4"

resolvers ++= Seq(
	Resolver.sonatypeRepo("releases"),
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.chuusai" 								%%	"shapeless" 									% "2.0.0",
	"com.github.tototoshi" 				%%	"scala-csv" 									% "1.1.2",
	"com.typesafe.scala-logging" 	%%	"scala-logging-slf4j"   			% "2.1.2",
	"ch.qos.logback" 							% 	"logback-classic" 						% "1.0.9",
	"com.github.scopt" 						%% 	"scopt" 											% "3.2.0",
	"com.typesafe.akka"          	%%	"akka-actor"             			% "2.3.2",
	"org.scala-lang.modules" 			%%	"scala-parser-combinators"		% "1.0.2",
	"com.typesafe.akka"          	%%	"akka-actor-tests"       			% "2.3.2" % "test",
	"com.typesafe.akka"          	%%	"akka-testkit"       					% "2.3.2" % "test",
	"org.scalatest" 							%		"scalatest_2.11" 							% "2.2.1" % "test",
	"org.scalamock" 							%%	"scalamock-scalatest-support"	% "3.2" 	% "test"
)

ScoverageKeys.minimumCoverage := 70

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.scoverageExcludedFiles := "*Shell*.*"

ScoverageKeys.excludedPackages := "deebee.exceptions"

ScoverageKeys.highlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else false
}

publishArtifact in Test := false

parallelExecution in Test := false

mainClass := Some("deebee.frontends.Shell")

AssemblyKeys.assemblyJarName in AssemblyKeys.assembly := s"deebee.jar"
