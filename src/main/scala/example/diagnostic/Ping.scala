package example.diagnostic

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.PlainText.ResponseBuilder._

object Ping {
  def route() = RouteSpec("Ping").at(Get) / "ping" bindTo Service.mk[Request, Response] { _ => Ok("pong") }
}
