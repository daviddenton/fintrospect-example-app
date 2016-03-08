package env

import java.time.{Clock, Instant, ZoneId}

import com.twitter.finagle.http.Request
import com.twitter.util.Await
import example.SecuritySystem

class TestEnvironment() {

  val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())

  val userDirectory = new FakeUserDirectory()
  val entryLogger = new FakeEntryLogger()

  val userDirectoryHttp = new InMemoryHttp(userDirectory)
  val entryLoggerHttp = new InMemoryHttp(entryLogger)

  private val securitySystemSvc = new SecuritySystem(
    userDirectoryHttp.service,
    entryLoggerHttp.service,
    clock).service

  def responseTo(request: Request) = {
    val msg = Await.result(securitySystemSvc(request))
    ResponseStatusAndContent(msg.status, msg.headerMap.getOrElse("Content-type", null), msg.contentString)
  }
}
