package example.api

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.{Request, Response, Status}
import example.external.EntryLogger
import example.{Inhabitants, Username}
import io.circe.generic.auto._
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query}
import io.fintrospect.{RouteSpec, ServerRoute}

import scala.language.reflectiveCalls

object ByeBye {
  def route(inhabitants: Inhabitants, entryLogger: EntryLogger): ServerRoute[Request, Response] = {
    val username = Query.required(ParameterSpec.string().map(s => Username(s), (u: Username) => u.value.toString), "username")

    val userExit = Service.mk[Request, Response] {
      request => {
        val exiting = username <-- request
        if (inhabitants.remove(exiting))
          entryLogger
            .exit(exiting)
            .map(_ => Accepted(encode(Message("processing"))))
        else BadRequest(encode(Message("User is not inside building")))
      }
    }

    RouteSpec("User exits the building")
      .taking(username)
      .returning(Status.Ok -> "Exit granted")
      .returning(Status.BadRequest -> "User is not inside building")
      .returning(Status.Unauthorized -> "Incorrect key")
      .at(Post) / "bye" bindTo userExit
  }
}
