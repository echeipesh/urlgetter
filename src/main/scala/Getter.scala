import akka.actor.{Status, Actor}
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Getter{
  case class Done()
  case class Abort()
}

class Getter(url: String, depth: Int) extends Actor {

  /*context exposes contextual information for the actor and the current message, such as:
      - factory methods to create child actors (actorOf)
      - system that the actor belongs to
      - parent supervisor
      - supervised children
      - lifecycle monitoring
      - hotswap behavior stack as described in Become/Unbecome
  */
  implicit val exec =
    context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  //NOTE: the actor dispatcher is able to run futures, like here:
  val future = WebClient.get(url)
  future onComplete {
    case Success(body) => self ! body
    case Failure(err) =>  self ! Status.Failure(err)
  }
  //The above block is equivalent to:
  //  import akka.pattern.pipe
  //  future.pipeTo(self)

  //The full block can then be prettified as:
  // WebClient get url pipeTo self

  def receive: Actor.Receive = {
    case body: String =>
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      stop()
    case Getter.Abort =>
        stop()
    case _: Status.Failure =>
      stop()
  }

  def stop(): Unit = {
    context.parent ! Getter.Done
    context.stop(self)
  }

  /**
   * @param body HTML text
   * @return Iterable for URLs in the body
   */
  def findLinks(body:String): Iterator[String]  = {
    val A_TAG = "(?i)<a ([^>]+)>.+?</a>".r
    val HREF_ATTR = """\s*(?i)href\s*=\s*(?:"([^"]*)"|'([^']*)'|([^'">\s]+))""".r

    for {
      anchor <- A_TAG.findAllMatchIn(body)
      HREF_ATTR(dquot, quot, bare)  <- anchor.subgroups
    } yield
      if (dquot != null) dquot
      else if (quot != null) quot
      else bare
  }
}