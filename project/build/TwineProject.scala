import sbt._
import Process._

class TwineProject(info: ProjectInfo) extends DefaultProject(info) {
  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
  val dispatch = "net.databinder" %% "dispatch-twitter" % "0.7.8-SNAPSHOT"
}
