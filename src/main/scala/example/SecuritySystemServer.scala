package example
import java.time.Clock

import com.twitter.finagle.{ListeningServer, _}
import com.twitter.util.Future

class SecuritySystemServer(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int) {

  private var server: ListeningServer = null

  val service = new SecuritySystem(
    Http.newService(s"localhost:$userDirectoryPort"),
    Http.newService(s"localhost:$entryLoggerPort"),
    Clock.systemUTC()).service

  def start() = {
    server = Http.serve(s":$serverPort", service)
    Future.Done
  }

  def stop() = server.close()
}
