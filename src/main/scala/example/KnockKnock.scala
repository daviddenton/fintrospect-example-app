package example


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status.{Accepted, BadRequest, NotFound, Ok, Unauthorized}
import com.twitter.finagle.http.{Request, Response}
import example.SecuritySystemAuth.apiKey
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.ResponseBuilder.implicits._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}

import scala.language.reflectiveCalls

class KnockKnock(inhabitants: Inhabitants, userDirectory: UserDirectory, entryLogger: EntryLogger) {
  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private val userEntry = Service.mk[Request, Response] {
    request =>
      userDirectory.lookup(username <-- request)
        .flatMap {
          case Some(user) =>
            if (inhabitants.add(user.name))
              entryLogger
                .enter(user.name)
                .map(ue => Accepted())
            else BadRequest()
          case None => NotFound()
        }
  }

  val route = RouteSpec("User enters the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .taking(username)
    .returning(Ok -> "Access granted")
    .returning(NotFound -> "Unknown user")
    .returning(BadRequest -> "User is already inside building")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "knock" bindTo userEntry
}
