organization := "urlgetter"

name := "urlgetter"

version := "0.1-SNAPSHOT"

libraryDependencies  ++= Seq(
	 "com.ning" % "async-http-client" % "1.7.19"
    , "com.typesafe.akka" %% "akka-actor" % "2.2.3"
    , "com.typesafe.akka" %% "akka-testkit" % "2.2.3"
    , "org.slf4j" % "slf4j-simple" % "1.6.4"
)