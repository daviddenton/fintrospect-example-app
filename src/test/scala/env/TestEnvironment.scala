package env

import java.time.{Clock, Instant, ZoneId}

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import example.{Event, SecuritySystem}
import io.fintrospect.testing.OverridableHttpService

import scala.collection.mutable

class TestEnvironment() {

  val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())

  val userDirectory = new FakeUserDirectory()
  val entryLogger = new FakeEntryLogger()

  val userDirectoryHttp = new OverridableHttpService[Response](userDirectory)
  val entryLoggerHttp = new OverridableHttpService[Response](entryLogger)

  val events = mutable.MutableList[Event]()

  private val securitySystemSvc = new SecuritySystem(
    userDirectoryHttp.service,
    entryLoggerHttp.service,
    events += _,
    clock).service

  def responseTo(request: Request) = {
    val msg = Await.result(securitySystemSvc(request))
    ResponseStatusAndContent(msg.status, msg.headerMap.getOrElse("Content-type", null), msg.contentString)
  }
}
