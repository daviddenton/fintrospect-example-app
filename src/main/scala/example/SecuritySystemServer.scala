package example
import java.time.Clock

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.configuration.{Authority, Port}

/**
  * Responsible for setting up real HTTP servers and clients to downstream services via HTTP
  */
class SecuritySystemServer(serverPort: Port, userDirectoryAuthority: Authority, entryLoggerAuthority: Authority) {

  private var server: ListeningServer = _

  private val service = new SecuritySystem(
    Http.newService(userDirectoryAuthority.toString()),
    Http.newService(entryLoggerAuthority.toString()),
    Clock.systemUTC()).service

  def start(): Future[Unit] = {
    server = Http.serve(s":$serverPort", service)
    Future.Done
  }

  def stop(): Future[Unit] = server.close()
}
