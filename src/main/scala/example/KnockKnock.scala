package example

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.{Request, Response, Status}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query}

import scala.language.reflectiveCalls

class KnockKnock(inhabitants: Inhabitants, userDirectory: UserDirectory, entryLogger: EntryLogger) {
  private val username = Query.required(ParameterSpec.string("username").map(s => Username(s), (u: Username) => u.value.toString))

  private val userEntry = Service.mk[Request, Response] {
    request =>
      userDirectory.lookup(username <-- request)
        .flatMap {
          case Some(user) =>
            if (inhabitants.add(user.name))
              entryLogger
                .enter(user.name)
                .map(ue => Accepted(encode(Message("Access granted"))))
            else BadRequest(encode(Message("User is already inside building")))
          case None => NotFound(encode(Message("Unknown user")))
        }
  }

  val route = RouteSpec("User enters the building")
    .taking(username)
    .returning(Status.Accepted -> "Access granted")
    .returning(Status.NotFound -> "Unknown user")
    .returning(Status.BadRequest -> "User is already inside building")
    .returning(Status.Unauthorized -> "Incorrect key")
    .at(Post) / "knock" bindTo userEntry
}
