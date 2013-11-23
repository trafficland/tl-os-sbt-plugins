resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

resolvers += Resolver.url("Artifactory Online", url("http://repo.scala-sbt.org/scalasbt/repo"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.trafficland" % "sbt-plugins" % "0.8.0")