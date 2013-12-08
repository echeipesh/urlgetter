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
  }

//what could we possibly need to do there?
//  override def postStop(): Unit = {
//    WebClient.shutdown()
//  }
}

import _root_.akka.Main
object Application extends App {
  Main.main(Array("Main"))
}