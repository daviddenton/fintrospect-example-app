package example.web

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import example.external.UserDirectory
import io.fintrospect.RouteSpec
import io.fintrospect.templating.View

class ShowKnownUsers(userDirectory: UserDirectory) {

  private val show = Service.mk[Request, View] { _ => userDirectory.list().flatMap(u => KnownUsers(u)) }

  val route = RouteSpec("See all known users").at(Get) / "known" bindTo show

}
