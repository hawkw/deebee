import com.typesafe.sbt.SbtGit._
import sbtassembly.AssemblyKeys
import scoverage.ScoverageSbtPlugin

versionWithGit

git.baseVersion := "0.1"

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

name := "deebee"

scalaVersion := "2.11.4"

resolvers ++= Seq(
	Resolver.sonatypeRepo("releases"),
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
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

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedFiles := "<empty>;Shell.*;DemoShell.*"

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;deebee.exceptions"


publishArtifact in Test := false

parallelExecution in Test := false

mainClass := Some("deebee.frontends.Shell")

AssemblyKeys.assemblyJarName in AssemblyKeys.assembly := s"deebee.jar"
