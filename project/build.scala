import sbt._
object MyApp extends Build
{
  lazy val root =
    Project("", file(".")) dependsOn(dispatchLiftJson,dispatchTwitter)
  lazy val dispatchTwitter =
    uri("git://github.com/n8han/dispatch-twitter#0.1.2")
  lazy val dispatchLiftJson =
    uri("git://github.com/dispatch/dispatch-lift-json#0.1.0")
}
