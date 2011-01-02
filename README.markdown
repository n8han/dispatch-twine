Dispatch Twine
==============

This is a sample application for Databinder Dispatch[1], to build and run
with simple-build-tool[2].

    $ sbt
    ...
    > update
    ...
    > run
    ... (authorize the app with a browser, note pin)
    > run <pin>
    > run

And you're streaming. (Tell your friends to tweet something, maybe.) There is not a 
graceful way to exit currently (forks welcome), so just ctrl+c to blow out of 
twine and sbt if you're done.

You can also pass in a tweet to post it.

    > run "Hey u guys I finally fixed up Dispatch's Twine example: https://github.com/n8han/dispatch-twine"

Note: Add "-Dfile.encoding=UTF-8" to your sbt start script if you want non-ASCII characters to render correctly.

[2]: http://code.google.com/p/simple-build-tool/
