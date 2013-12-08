import akka.actor._
import scala.concurrent.duration._

class Main extends Actor {
  import Receptionist._

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")

  receptionist ! Get("http://www.google.com")

  context.setReceiveTimeout(10.seconds)

  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url':\n", "\n", "\n"))
    case Failed(url) =>
      println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      println("STOP STOP SOP!")
      context.stop(self)
      //discussion on shutdown patterns: http://letitcrash.com/post/30165507578/shutdown-patterns-in-akka-2
      context.system.shutdown()
  }

  //The postStop hook is invoked after an actor is fully stopped. This enables cleaning up of resources
  //http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  override def postStop(): Unit = {
    WebClient.shutdown()
  }
}

object Main extends App {
  println("Starting actors...")
  val system = ActorSystem("UrlGetter")
  val myActor = system.actorOf(Props[Main], name = "MainActor")
}