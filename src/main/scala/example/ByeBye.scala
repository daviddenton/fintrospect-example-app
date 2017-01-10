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

class ByeBye(inhabitants: Inhabitants, entryLogger: EntryLogger) {

  private val username = Query.required(ParameterSpec.string("username").map(s => Username(s), (u: Username) => u.value.toString))

  private val userExit = Service.mk[Request, Response] {
    request => {
      val exiting = username <-- request
      if (inhabitants.remove(exiting))
        entryLogger
          .exit(exiting)
          .map(_ => Accepted(encode(Message("processing"))))
      else BadRequest(encode(Message("User is not inside building")))
    }
  }

  val route = RouteSpec("User exits the building")
    .taking(username)
    .returning(Status.Ok -> "Exit granted")
    .returning(Status.BadRequest -> "User is not inside building")
    .returning(Status.Unauthorized -> "Incorrect key")
    .at(Post) / "bye" bindTo userExit
}
