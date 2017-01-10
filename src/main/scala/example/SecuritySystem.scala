package example

import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import example.api.Api
import example.diagnostic.Diagnostic
import example.external.{EntryLogger, UserDirectory}
import example.web.Web
import io.fintrospect.Module.combine

/**
  * Sets up the business-level API for the application. Note that the generic clients on the constructor allow us to
  * inject non-HTTP versions of the downstream dependencies so we can run tests without starting up real HTTP servers.
  */
class SecuritySystem(userDirectoryHttp: Service[Request, Response], entryLoggerHttp: Service[Request, Response], clock: Clock) {

  private val userDirectory = new UserDirectory(userDirectoryHttp)
  private val entryLogger = new EntryLogger(entryLoggerHttp, clock)
  private val inhabitants = new Inhabitants

  val service: Service[Request, Response] = combine(
    Api.module(inhabitants, userDirectory, entryLogger),
    Diagnostic.module(),
    Web.module(userDirectory)
  ).toService
}
