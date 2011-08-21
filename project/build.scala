import sbt._
object MyApp extends Build
{
  lazy val root =
    Project("", file(".")) dependsOn(dispatchTwitter)
  lazy val dispatchTwitter =
    uri("git://github.com/n8han/dispatch-twitter#0.1.2")
}
