Dispatch Twine
==============

This is a sample application for [Dispatch][1], to build and run
with [simple-build-tool][2].

[1]: http://dispatch.databinder.net/

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

### Character Encodings
Add "-Dfile.encoding=UTF-8" to your sbt start script if you want non-ASCII characters to render in utf-8, which your terminal may correctly display.

[2]: http://code.google.com/p/simple-build-tool/
