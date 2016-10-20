package example


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import example.SecuritySystemAuth.apiKey
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder.implicits._

import scala.language.reflectiveCalls

class WhoIsThere(inhabitants: Inhabitants, userDirectory: UserDirectory) {

  private val listUsers = Service.mk[Request, Response] {
    request =>
      Future.collect(inhabitants.map(userDirectory.lookup).toSeq)
        .map(_.flatten[User])
        .map(us => Ok(encode(us)))
  }

  val route = RouteSpec("List current users in the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .returning(Ok(encode(Seq(User(Id(1), Username("A user"), EmailAddress("user@bob.com"))))))
    .at(Get) / "whoIsThere" bindTo listUsers
}
