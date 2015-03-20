import sbt._

object ZuthaBuild extends Build {

  lazy val root = Project(
    "Zutha",
    file("."))

  System.setProperty("myproject.root", "D:\\Dropbox\\Projects\\Programming\\Zutha")
}
