import sbt._
import Process._

class TwineProject(info: ProjectInfo) extends DefaultProject(info) {
  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
  val dvers = "0.8.0"
  val twitter = "net.databinder" %% "dispatch-twitter" % dvers
  val nio = "net.databinder" %% "dispatch-nio" % dvers
}
