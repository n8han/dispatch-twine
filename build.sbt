libraryDependencies ~= { seq =>
  val vers = "0.8.6"
  seq ++ Seq(
    "net.databinder" %% "dispatch-core" % vers,
    "net.databinder" %% "dispatch-oauth" % vers,
    "net.databinder" %% "dispatch-nio" % vers,
    /* Twine doesn't need the below dependency, but it simplifies
     * the Dispatch tuturials to keep it here for now. */
    "net.databinder" %% "dispatch-http" % vers
  )
}

initialCommands := "import dispatch._"