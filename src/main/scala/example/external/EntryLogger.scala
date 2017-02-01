package example.external

import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import example.external.EntryLogger.{Entry, Exit, LogList}
import example.{UserEntry, Username}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters.Body


object EntryLogger {

  object Entry {
    val body = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(body).at(Post) / "entry"
  }

  object Exit {
    val body = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(body).at(Post) / "exit"
  }

  object LogList {
    val route = RouteSpec().at(Get) / "list"
    val response = Body(bodySpec[Seq[UserEntry]]())
  }

}

/**
  * Remote Entry Logger service, accessible over HTTP
  */
class EntryLogger(client: Service[Request, Response], clock: Clock) {

  private def expectStatusAndExtract[T](expectedStatus: Status, body: Body[T]): Response => Future[T] =
    request =>
      if (request.status == expectedStatus) Future(body <-- request)
      else Future.exception(RemoteSystemProblem("entry logger", request.status))

  private val entryClient = Entry.route bindToClient client

  def enter(username: Username): Future[UserEntry] =
    entryClient(Entry.body --> UserEntry(username.value, goingIn = true, clock.instant().toEpochMilli))
      .flatMap(expectStatusAndExtract(Created, Entry.body))

  private val exitClient = Exit.route bindToClient client

  def exit(username: Username): Future[UserEntry] =
    exitClient(Exit.body --> UserEntry(username.value, goingIn = false, clock.instant().toEpochMilli))
      .flatMap(expectStatusAndExtract(Created, Exit.body))

  private val listClient = LogList.route bindToClient client

  def list(): Future[Seq[UserEntry]] = listClient().flatMap(expectStatusAndExtract(Ok, LogList.response))
}
