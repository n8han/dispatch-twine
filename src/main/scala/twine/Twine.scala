/***************~~~~~~~~~~~~~~~~~~~SCALA~~~~~~~~~~~~~~~~~~~***************\
 ***************                                           ***************
 ***************   Twine, a command line Twitter client!   ***************
 ***************                                           ***************
\***************~~~~~~~~~~~~~~~~~~~ALACS~~~~~~~~~~~~~~~~~~~************* */

// this is one way to declare your packages in Scala
package dispatch {
  // the three imports below use the current `dispatch` scope!
  import json.JsHttp._
  import oauth._
  import twitter._
  
  package twine {
    // this singleton object is the application
    object Twine {
      // import and nickname Configgy's main access object
      import _root_.net.lag.configgy.{Configgy => C}
      // import all the methods, including implicit conversions, defined on dispatch.Http
      import Request._

      // this will be our datastore
      val conf = new java.io.File(System.getProperty("user.home"), ".twine.conf")
      // OAuth application key, top-secret
      val consumer = Consumer("lrhF8SXnl5q3gFOmzku4Gw", "PbB4Mr8pKAChWmd6AocY6gLmAKzPKaszYnXyIDQhzE")
      // one nio http access point, please!
      val http = new nio.Http

      // ---BY YOUR COMMAND---
      def main(args: Array[String]) {
        // create config file if it doesn't exist
        conf.createNewFile()
        // read config file to C.config
        C.configure(conf.getPath)
        
        // This is it, people. All paths return to println with a message for the user,
        // except `cat` which doesn't. We're going to pattern-match against both the
        // parameter sequence and a Token that is either there or not there. The
        // dispatch.oauth.Token(m: Map[...]) method knows about maps with token keys
        // in them. If these are present under "access", we'll get Some(token)
        
        println( (args, Token(C.config.configMap("access").asMap)) match {
          // the only parameter was "reset"; ignore the token and delete the data store
          case (Array("reset"), _) => conf.delete(); "OAuth credentials deleted."
          // there are no parameters, but we have a token! Go into `cat`, forever.
          case (Array(), Some(tok)) => cat(tok)
          // there are some parameters and a token, combine parameters and...
          case (args, Some(tok)) => (args mkString " ") match {
            // dang tweet is too long
            case tweet if tweet.length > 140 => 
              "%d characters? This is Twitter not NY Times Magazine." format tweet.length
            // it looks like an okay tweet, let us post it:
            case tweet => http(Status.update(tweet, consumer, tok) ># { js =>
              // handling the Status.update response as JSON, we take what we want
              val Status.user.screen_name(screen_name) = js
              val Status.id(id) = js
              // this goes back to our user
              "Posted: " + (Twitter.host / screen_name / "status" / id.toString to_uri)
            })
          }
          // there was no access token, we must still be in the oauthorization process
          case _ => get_authorization(args)
        })
        http.shutdown()
      }
      def cat(token: Token) = {
        // get us some tweets
        val fut = http(UserStream.open(consumer, token, None) { message => 
          import net.liftweb.json.JsonAST._
          // this listener is called each time a json message arrives

          // the friends message should be the first one to come in
          for (JArray(friends) <- message \ "friends")
            print("Streaming tweets as they arrive, press [↵ Enter] to stop...")

          // print apparent tweet if it has text and a screen_name
          for {
            JString(text) <- message \ "text"
            JString(name) <- message \ "user" \ "screen_name"
          } yield
            print("\n%-15s%s" format (name, text) )
        } ^! { 
          case exc => System.err.println("Connection error: " + exc.getMessage)
        })
        // wait here until the user pushes some buttons
        while (System.in.available <= 0 && !fut.isSet)
          Thread.sleep(1000)
        fut.stop()
        "Okay!"
      }
      // oauth sesame
      def get_authorization(args: Array[String]) = {
        // this time we are matching against a potential request token
        ((args, Token(C.config.configMap("request").asMap)) match {
          // one parameter that must be the verifier, and there's a request token
          case (Array(verifier), Some(tok)) => try {
            // exchange it for an access token
            http(Auth.access_token(consumer, tok, verifier))() match {
              case (access_tok, _, screen_name) =>
                // nb: we're producing a message, a token type name, and the token itself
                ("Approved! It's tweetin' time, %s." format screen_name, Some(("access", access_tok)))
            } } catch {
              // accidents happen
              case StatusCode(401, _) =>
                // no token for you
                ("Rats! That PIN %s doesn't seem to match." format verifier, None)
            }
          // there wasn't a parameter so who cares if we have a request token, just get a new one
          case _ => 
            // a request token for the Twine application, kthxbai
            val tok = http(Auth.request_token(consumer))()
            // generate the url the user needs to go to, to grant us access
            val auth_uri = Auth.authorize_url(tok).to_uri
            (( try {
              // try to open it with the fancy desktop integration stuff,
              // using reflection so we can still compile on Java 5
              val dsk = Class.forName("java.awt.Desktop")
              dsk.getMethod("browse", classOf[java.net.URI]).invoke(
                dsk.getMethod("getDesktop").invoke(null), auth_uri
              )
              "Accept the authorization request in your browser, for the fun to begin."
            } catch {
              // THAT went well. We'll just have to pass on that link to be printed.
              case _ =>
                "Open the following URL in a browser to permit this application to tweet 4 u:\n%s".
                  format(auth_uri.toString)
            }) + // so take one of those two messages and a general message
              "\n\nThen run `twine <pin>` to complete authorization.\n",
              // and the request token that we got back from Twitter
              Some(("request", tok)))
        }) match // against the tuple produced by the last match
        {
          // no token, just a message to be printed up in `main`
          case (message, None) => message
          // a token of some kind: we should save this in the datastore perhaps
          case (message, Some((name, tok))) =>
            val conf_writer = new java.io.FileWriter(conf)
            // let us also take this opportunity to set the log level
            conf_writer write (
            """ |<log>
                |  level = "WARNING"
                |  console = true
                |</log>
                |<%s>
                |  oauth_token = "%s"
                |  oauth_token_secret = "%s"
                |</%s>""".stripMargin format (name, tok.value, tok.secret, name)
            )
            conf_writer.close
            message // for you sir!
        }
      }         // get_authorization
    }          // Twine
  }           // twine
}            // dispatch
            // earth
