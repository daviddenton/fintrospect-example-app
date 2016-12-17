package example

import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import example.EntryLogger.{Entry, Exit, LogList}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters.Body


object EntryLogger {

  object Entry {
    val entry = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(entry).at(Post) / "entry"
  }

  object Exit {
    val entry = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(entry).at(Post) / "exit"
  }

  object LogList {
    val route = RouteSpec().at(Get) / "list"
  }

}

/**
  * Remote Entry Logger service, accessible over HTTP
  */
class EntryLogger(client: Service[Request, Response], clock: Clock) {

  private def expectStatusAndExtract[T](expectedStatus: Status, b: Body[T]): Response => Future[T] = {
    r =>
      if (r.status == expectedStatus) Future.value(b <-- r)
      else Future.exception(RemoteSystemProblem("entry logger", r.status))
  }

  private val entryClient = Entry.route bindToClient client

  def enter(username: Username): Future[UserEntry] =
    entryClient(Entry.entry --> UserEntry(username.value, goingIn = true, clock.instant().toEpochMilli))
      .flatMap(expectStatusAndExtract(Created, Entry.entry))

  private val exitClient = Exit.route bindToClient client

  def exit(username: Username): Future[UserEntry] =
    exitClient(Exit.entry --> UserEntry(username.value, goingIn = false, clock.instant().toEpochMilli))
      .flatMap(expectStatusAndExtract(Created, Exit.entry))

  private val listClient = LogList.route bindToClient client

  def list(): Future[Seq[UserEntry]] = {
    listClient().flatMap(expectStatusAndExtract(Ok, Body(bodySpec[Seq[UserEntry]]())))
  }
}
