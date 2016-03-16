package example

import java.time.LocalDateTime

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoutes}

case class Index(time: String, browser: String) extends View

class ShowIndex(userDirectory: UserDirectory) extends ServerRoutes[Request, View] {

  private val index = Service.mk[Request, View] {
    request => Index(LocalDateTime.now().toString, request.headerMap.getOrElse("User-Agent", "unknown"))
  }

  add(RouteSpec("Index").at(Get) bindTo index)
}
