package example

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import example.SecuritySystemAuth._
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

class WhoIsThere(inhabitants: Inhabitants, userDirectory: UserDirectory) extends ServerRoutes[Response] {

  private def listUsers() = Service.mk[Request, Response] {
    request =>
      Future.collect(inhabitants.map(userDirectory.lookup).toSeq)
        .map(_.flatten[User])
        .map(us => Ok(encode(us)))
  }

  add(RouteSpec("List current users in the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .returning(Ok(encode(Seq(User(Id(1), Username("A user"), EmailAddress("user@bob.com"))))))
    .at(Get) / "whoIsThere" bindTo listUsers)
}
