import com.typesafe.startscript.StartScriptPlugin

//logLevel := Level.Debug

name := "Zutha"

version := "0.0.1-SNAPSHOT"

organization := "net.zutha"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

//--------- web plugin ---------
seq(webSettings :_*)

//--------- start script plugin ---------
seq(StartScriptPlugin.startScriptForClassesSettings: _*)

//--------- web server settings ---------
port in container.Configuration := 8082

//--------- OneJar plugin ---------
//seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

//libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

//--------- War Plugins ---------
//seq(jettyEmbedSettings:_*)

// for Sbt Eclipse
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

EclipseKeys.withSource := true

//for JRebel
scanDirectories in Compile := Nil

resolvers ++= Seq(
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "fyrie snapshots" at "http://repo.fyrie.net/snapshots",
    "tmlab" at "http://maven.topicmapslab.de/public",
    "org.tmapi" at "http://www.tmapi.org/maven-repository/snapshots",
    "semagia" at "http://repository.semagia.com/releases"
)

libraryDependencies ++= {
    val liftVersion = "2.4-M5"
    val majortom_version = "1.2.0"
    Seq(
    "org.eclipse.jetty" % "jetty-webapp" % "8.0.0.v20110901" % "container",
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
    "org.slf4j" % "slf4j-jdk14" % "1.6.4",
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-widgets" % liftVersion % "compile",
    "net.liftweb" %% "lift-openid" % liftVersion % "compile",
    "net.liftweb" %% "lift-json" % liftVersion % "compile",
    "redis.clients" % "jedis" % "2.0.0",
    //"com.typesafe.akka" % "akka-actor" % "2.0",
    //"net.fyrie" %% "fyrie-redis" % "2.0-SNAPSHOT",
    "net.debasishg" %% "redisclient" % "2.4.2-SNAPSHOT",
    "de.topicmapslab.majortom" % "majortom-model" % majortom_version,
//    "de.topicmapslab.majortom" % "majortom-db" % majortom_version,
//    "de.topicmapslab.majortom" % "majortom-inMemory" % majortom_version,
//    "de.topicmapslab.majortom" % "majortom-queued" % majortom_version,
    "de.topicmapslab.majortom" % "majortom-redis" % majortom_version,
    "org.tmapix" % "tmapix-io" % "1.0.0"
)}

