package example.web

import java.time.LocalDateTime

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import example.external.UserDirectory
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoute}

case class Index(time: String, browser: String) extends View

object ShowIndex {
  def route(userDirectory: UserDirectory): ServerRoute[Request, View] = {
    val service = Service.mk[Request, View] {
      request => Index(LocalDateTime.now().toString, request.headerMap.getOrElse("User-Agent", "unknown"))
    }
    RouteSpec("Index").at(Get) bindTo service
  }
}
