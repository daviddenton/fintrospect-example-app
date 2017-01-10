package example.diagnostic

import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.{RouteSpec, ServerRoute}

object Uptime {
  def route(clock: Clock): ServerRoute[Request, Response] = {
    val startTime = clock.instant().toEpochMilli
    RouteSpec("Uptime monitor").at(Get) / "uptime" bindTo Service.mk[Request, Response] { _ =>
      Ok(s"uptime is: ${(clock.instant().toEpochMilli - startTime) / 1000}s")
    }
  }
}
