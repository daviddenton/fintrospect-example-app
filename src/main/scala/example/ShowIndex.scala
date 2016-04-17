package example

import java.time.LocalDateTime

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.RouteSpec
import io.fintrospect.templating.View

case class Index(time: String, browser: String) extends View

class ShowIndex(userDirectory: UserDirectory) {

  private val index = Service.mk[Request, View] {
    request => Index(LocalDateTime.now().toString, request.headerMap.getOrElse("User-Agent", "unknown"))
  }

  val route = RouteSpec("Index").at(Get) bindTo index
}
