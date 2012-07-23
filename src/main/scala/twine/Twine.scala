/***************~~~~~~~~~~~~~~~~~~~SCALA~~~~~~~~~~~~~~~~~~~***************\
 ***************                                           ***************
 ***************   Twine, a command line Twitter client!   ***************
 ***************                                           ***************
\***************~~~~~~~~~~~~~~~~~~~ALACS~~~~~~~~~~~~~~~~~~~************* */

package dispatch.twine

import dispatch._

import net.liftweb.json.JsonAST._

package as {
  import net.liftweb.json.JsonParser.parse
  val JValue = dispatch.as.string.andThen(parse)
  package stream {
    object JValue {
      def apply[U](f: JValue => U) =
        new stream.StringsByLine[Unit] {
          def onStringBy(string: String) {
            f(parse(string))
          }
          def onCompleted = ()
        }
    }
  }
}
trait TwitterEndpoints extends oauth.SomeEndpoints {
  val requestToken = "http://api.twitter.com/oauth/request_token"
  val accessToken  = "http://api.twitter.com/oauth/access_token"
  val authorize    = "http://api.twitter.com/oauth/authorize"
}
trait TwitterAuth extends oauth.Exchange
  with oauth.SomeCallback 
  with oauth.SomeHttp
  with oauth.SomeConsumer
  with TwitterEndpoints
object Auth extends TwitterAuth {
  val http = Twine.http
  // use pin-based, out-of-band authentication
  val callback = "oob"
  // OAuth application key, top-secret
  val consumer = new com.ning.http.client.oauth.ConsumerKey(
    "lrhF8SXnl5q3gFOmzku4Gw",
    "PbB4Mr8pKAChWmd6AocY6gLmAKzPKaszYnXyIDQhzE"
  )
}

// this singleton object is the application
object Twine {
  // this will be our datastore
  val conf = new java.io.File(System.getProperty("user.home"),
                              ".twine.properties")
  val props = new java.util.Properties
  def token_type = props.get("token_type")
  def token = Token(props)
  // one nio http access point, please!
  val http = Http

  // ---BY YOUR COMMAND---
  def main(args: Array[String]) {
    // create config file if it doesn't exist
    conf.createNewFile()
    // read properties file
    props.load(new java.io.FileInputStream(conf))

    // This is it, people. All paths return to println with a message for the user,
    // except `cat` which doesn't. We're going to pattern-match against both the
    // parameter sequence and a Token that is either there or not there. The
    // dispatch.oauth.Token(m: Map[...]) method knows about maps with token keys
    // in them. If these are present under "access", we'll get Some(token)

    println( (args, token_type, token) match {
      // the only parameter was "reset"; ignore the token and delete the data store
      case (Array("reset"), _, _) => conf.delete() "OAuth credentials deleted."
      // there are no parameters, but we have a token! Go into `cat`, forever.
      case (Array(), "access", Some(tok)) => cat(tok)
      // there are some parameters and a token, combine parameters and...
      case (args, "access", Some(tok)) => (args mkString " ") match {
        // dang tweet is too long
        case tweet if tweet.length > 140 => 
          "%d characters? This is Twitter not NY Times Magazine.".format(
            tweet.length
          )
        // it looks like an okay tweet, let us post it:
/*        case tweet =
          val update = http(Status.update(tweet, consumer, tok) as.JValue)
          for (js <- update) yield {
              // extract what we want from the json
              val Status.user.screen_name(screen_name) = js
              val Status.id(id) = js
              // this goes back to our user
              "Posted: " + (Twitter.host / screen_name / "status" / 
                            id.toString to_uri)
        })()*/
      }
      // there was no access token, we must still be in the oauthorization process
      case _ => get_authorization(args)
    })
    http.shutdown()
  }
  def cat(token: Token) = {
    // get us some tweets
    val stream = http(url("https://userstream.twitter.com/2/user.json") <@
      (Auth.consumer, token) OK as.stream.json { message =>
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
      }
    )
    // wait here until the user pushes some buttons
    while (System.in.available <= 0 && !stream.isComplete)
      Thread.sleep(1000)
    stream.abort()
    "Okay!"
  }
  // oauth sesame
  def get_authorization(args: Array[String]) = {
    // this time we are matching against a potential request token
    ((args, token_type, token) match {
      // one parameter that must be the verifier, and there's a request token
      case (Array(verifier), request, Some(tok)) =>
        try {
          // exchange it for an access token
          http(Auth.access_token(consumer, tok, verifier) ~> {
            case (access_tok, _, screen_name) =>
              // nb: we're producing a message, a token type name, and the token itself
              ("Approved! It's tweetin' time, %s." format screen_name, Some(("access", access_tok)))
          })()
        } catch {
          // accidents happen
          case StatusCode(401, _) =>
            // no token for you
            ("Rats! That PIN %s doesn't seem to match." format verifier, None)
        }
      // there wasn't a parameter so who cares if we have a request token, just get a new one
      case (a,b,c) => 
        println("%s %s %s".format(a,b,c))
        // a request token for the Twine application, kthxbai
        val tok = http(Auth.request_token(consumer))()
        println(tok)
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
        conf_writer.write(
        """ |oauth_token        = %s
            |oauth_token_secret = %s
            |token_type         = %s""".stripMargin.format(
              tok.value, tok.secret, name
            )
        )
        conf_writer.close
        message // for you sir!
    }
  }         // get_authorization
}          // Twine
          // earth
