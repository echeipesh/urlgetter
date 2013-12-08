import akka.actor.{ActorRef, Actor}
import akka.pattern.pipe
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext

object Cache {
  case class Get(url: String)
  case class Result(client: ActorRef, url: String, body: String)
}
class Cache extends Actor {
  var cache = Map.empty[String, String]

  def receive = {
    case Cache.Get(url) =>
      if (cache contains url) sender ! cache(url)
      else {
        implicit val exec =
          context.dispatcher.asInstanceOf[Executor with ExecutionContext]

        /*
        WebClient get url foreach { body =>
          //NOTE: WebClient get url gets a future
          //this happens outside of actor scope, in the future callback ~!~
          cache += url -> body
          sender ! body
        }
        */

        val client = sender
        //we cache this ActorRef so it is available in Future, with current value
        WebClient get url map (Cache.Result(client, url, _)) pipeTo self
        //THIS would be bad: ??? why exactly?
        //WebClient get url map (Cache.Result(sender, url, _)) pipeTo self
      }
    case Cache.Result(client, url, body) =>
      cache += url -> body
      client ! body
  }
}
