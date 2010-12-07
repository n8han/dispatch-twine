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
twine and sbt if you're done. You can also pass in a tweet to post it.

The End.

[2]: http://code.google.com/p/simple-build-tool/