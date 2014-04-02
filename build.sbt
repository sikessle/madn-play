import play.Project._

name := """MADN-play"""

version := "1.0-SNAPSHOT"

resolvers += "HTWG Resolver" at "http://lenny2.in.htwg-konstanz.de:8081/artifactory/libs-snapshot-local"

libraryDependencies ++= Seq(
	"org.webjars" %% "webjars-play" % "2.2.0", 
	"org.webjars" % "bootstrap" % "2.3.1",
	"MADN" % "de.htwg.sa.madn" % "0.1.0-SNAPSHOT")

playJavaSettings




