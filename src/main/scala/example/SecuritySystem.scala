package example

import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import example.api.Api
import example.diagnostic.{Auditor, Diagnostic}
import example.external.{EntryLogger, UserDirectory}
import example.web.Web
import io.fintrospect.Module
import io.fintrospect.Module.combine

/**
  * Sets up the business-level API for the application. Note that the generic clients on the constructor allow us to
  * inject non-HTTP versions of the downstream dependencies so we can run tests without starting up real HTTP servers.
  */
class SecuritySystem(userDirectoryHttp: Service[Request, Response],
                     entryLoggerHttp: Service[Request, Response],
                     events: Events,
                     clock: Clock) {

  private val userDirectory = new UserDirectory(userDirectoryHttp)
  private val entryLogger = new EntryLogger(entryLoggerHttp, clock)
  private val inhabitants = new Inhabitants

  private val module: Module = combine(
    Api.module(Root / "security", inhabitants, userDirectory, entryLogger),
    Diagnostic.module(Root / "internal", clock),
    Web.module(Root, userDirectory)
  )

  val service: Service[Request, Response] = Auditor(clock, events).andThen(module.toService)
}
