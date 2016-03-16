package example

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import example.SecuritySystemAuth.apiKey
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

class ByeBye(inhabitants: Inhabitants, entryLogger: EntryLogger) extends ServerRoutes[Request, Response] {

  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private val userExit = Service.mk[Request, Response] {
    request => {
      val exiting = username <-- request
      if (inhabitants.remove(exiting))
        entryLogger
          .exit(exiting)
          .map(_ => Accepted())
      else BadRequest()
    }
  }

  add(RouteSpec("User exits the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .taking(username)
    .returning(Ok -> "Exit granted")
    .returning(BadRequest -> "User is not inside building")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "bye" bindTo userExit)
}
