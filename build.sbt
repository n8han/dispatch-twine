libraryDependencies ~= { seq =>
  val vers = "0.8.7"
  seq ++ Seq(
    "net.databinder" %% "dispatch-core" % vers,
    "net.databinder" %% "dispatch-oauth" % vers,
    "net.databinder" %% "dispatch-nio" % vers,
    /* Twine doesn't need the below dependencies, but it simplifies
     * the Dispatch tutorials to keep it here for now. */
    "net.databinder" %% "dispatch-http" % vers,
    "net.databinder" %% "dispatch-tagsoup"% vers
  )
}

initialCommands := "import dispatch._"