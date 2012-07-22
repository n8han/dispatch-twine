libraryDependencies ~= { seq =>
  val vers = "0.9.0-beta2"
  seq ++ Seq(
    "net.databinder.dispatch" %% "core" % vers
  )
}

initialCommands := "import dispatch._"