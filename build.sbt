libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-core" % "0.8.5",
  "net.databinder" %% "dispatch-oauth" % "0.8.5",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5",
  "net.databinder" %% "dispatch-nio" % "0.8.5",
  /* Twine doesn't need the below dependency, but it simplifies
   * the Dispatch tuturials to keep it here for now. */
  "net.databinder" %% "dispatch-http" % "0.8.5"
)

initialCommands := "import dispatch._"