package example

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoutes}

class ShowKnownUsers(userDirectory: UserDirectory) extends ServerRoutes[View] {

  private def show() = Service.mk[Request, View] {
    request => userDirectory.list().flatMap(u => KnownUsers(u))
  }

  add(RouteSpec("See current inhabitants of building").at(Get) / "known" bindTo show)

}
