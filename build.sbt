libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-twitter" % "0.8.3",
  "net.databinder" %% "dispatch-nio" % "0.8.3",
  /* Twine doesn't need the below dependency, but it simplifies
   * the Dispatch tuturials to keep it here for now. */
  "net.databinder" %% "dispatch-http" % "0.8.3"
)
