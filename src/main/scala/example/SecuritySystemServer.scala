package example
import java.time.Clock

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future

/**
  * Responsible for setting up real HTTP servers and clients to downstream services via HTTP
  */
class SecuritySystemServer(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int) {

  private var server: ListeningServer = _

  private val service = new SecuritySystem(
    Http.newService(s"localhost:$userDirectoryPort"),
    Http.newService(s"localhost:$entryLoggerPort"),
    Clock.systemUTC()).service

  def start(): Future[Unit] = {
    server = Http.serve(s":$serverPort", service)
    Future.Done
  }

  def stop(): Future[Unit] = server.close()
}
