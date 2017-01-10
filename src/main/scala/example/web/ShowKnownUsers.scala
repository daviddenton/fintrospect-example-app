package example.web

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import example.User
import example.external.UserDirectory
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoute}

case class KnownUsers(users: Seq[User]) extends View

object ShowKnownUsers {
  def route(userDirectory: UserDirectory): ServerRoute[Request, View] = {
    val service = Service.mk[Request, View] { _ => userDirectory.list().flatMap(u => KnownUsers(u)) }

    RouteSpec("See all known users").at(Get) / "known" bindTo service
  }
}
