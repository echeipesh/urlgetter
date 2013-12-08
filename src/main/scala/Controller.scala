import akka.actor._
import scala.concurrent.duration._

object Controller{
  case class Check(s: String, i: Int)
  case class Result(s: Set[String])
  case class Done()
}

class Controller extends Actor with ActorLogging{
  //this is reset every time we get a message, else sends us ReceiveTimeout
  context.setReceiveTimeout(10.seconds)

  var cache = Set.empty[String]
  var children = Set.empty[ActorRef]
  def receive = {

    case Controller.Check(url, depth) =>
      println(s"CHECKING: $depth checking $url")
      log.debug("{} checking {}", depth, url)

      if (!cache(url) && depth>0)
        children += context.actorOf( Props(classOf[Getter], url, depth-1) )
        //above is the prefered way according to the docs, avoid closing over this scope
        //although it does seem to turn a compile-time error into run-time error
        //children += context.actorOf( Props(new Getter(url, depth - 1)) )
      cache += url

    case Getter.Done =>
      children -= sender
      if (children.isEmpty)
        context.parent ! Controller.Result(cache)

    case ReceiveTimeout =>
      children foreach (_ ! Getter.Abort)
      context.parent ! Controller.Done
      context.stop(self)
  }
}
