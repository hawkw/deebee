resolvers += Classpaths.sbtPluginReleases

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "0.99.10")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.99.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")