package example.diagnostic

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.{RouteSpec, ServerRoute}

object Ping {
  def route(): ServerRoute[Request, Response] =
    RouteSpec("Uptime monitor").at(Get) / "ping" bindTo Service.mk[Request, Response] { _ => Ok("pong") }
}
