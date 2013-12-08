import com.ning.http.client.AsyncHttpClient
import java.util.concurrent.Executor
import scala.concurrent.{Promise, Future}

class  BadStatus(code: Int) extends RuntimeException("Bad Status: $code")

object WebClient {
  val client = new AsyncHttpClient


  def get(url: String)(implicit exec: Executor):Future[String] = {
    val f = client.prepareGet(url).execute()
    val p = Promise[String]()
    f.addListener(new Runnable {
      def run = {
        val response = f.get
        if (response.getStatusCode <400)
          p.success(response.getResponseBodyExcerpt(131072))
        else
          p.failure(new BadStatus(response.getStatusCode))
      }
    }, exec)
    p.future
  }

  //close the underlying connections
  def shutdown(): Unit = client.close()
}