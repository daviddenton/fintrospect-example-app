package example.api

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import example.external.UserDirectory
import example.{EmailAddress, Id, Inhabitants, User, Username}
import io.circe.generic.auto._
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.{RouteSpec, ServerRoute}

object WhoIsThere {
  def route(inhabitants: Inhabitants, userDirectory: UserDirectory): ServerRoute[Request, Response] = {
    val listUsers = Service.mk[Request, Response] {
      _ =>
        Future.collect(inhabitants.map(userDirectory.lookup).toSeq)
          .map(_.flatten[User])
          .map(us => Ok(encode(us)))
    }

    RouteSpec("List current users in the building")
      .returning(Ok(encode(Seq(User(Id(1), Username("A user"), EmailAddress("user@bob.com"))))))
      .at(Get) / "whoIsThere" bindTo listUsers
  }
}
