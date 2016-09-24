package example


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Json4s.ResponseBuilder.implicits._

class Ping {
  private val pong = Service.mk[Request, Response] { _ => Ok("pong") }

  val route = RouteSpec("Uptime monitor").at(Get) / "ping" bindTo pong
}
