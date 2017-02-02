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
import io.fintrospect.filters.ResponseFilters.ExtractingResponse
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters.Body
import io.fintrospect.util.Extracted


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
    val response = Body(bodySpec[Seq[UserEntry]]())
    val route = RouteSpec().at(Get) / "list"
  }

}

/**
  * Remote Entry Logger service, accessible over HTTP
  */
class EntryLogger(client: Service[Request, Response], clock: Clock) {

  private val entryClient = Entry.route bindToClient ExtractingResponse(Entry.body).andThen(OnlyAccept(Created)).andThen(client)

  def enter(username: Username): Future[UserEntry] =
    entryClient(Entry.body --> UserEntry(username.value, goingIn = true, clock.instant().toEpochMilli))
      .flatMap {
        case Extracted(Some(userEntry)) => Future(userEntry)
        case _ => Future.exception(RemoteSystemProblem("enter", Status.BadGateway))
      }

  private val exitClient = Exit.route bindToClient ExtractingResponse(Exit.body).andThen(OnlyAccept(Created)).andThen(client)

  def exit(username: Username): Future[UserEntry] =
    exitClient(Exit.body --> UserEntry(username.value, goingIn = false, clock.instant().toEpochMilli))
      .flatMap {
        case Extracted(Some(userEntry)) => Future(userEntry)
        case _ => Future.exception(RemoteSystemProblem("exit", Status.BadGateway))
      }

  private val listClient = LogList.route bindToClient ExtractingResponse(LogList.response).andThen(OnlyAccept(Ok)).andThen(client)

  def list(): Future[Seq[UserEntry]] =
    listClient().flatMap {
      case Extracted(Some(logs)) => Future(logs)
      case _ => Future.exception(RemoteSystemProblem("logs", Status.BadGateway))
    }
}
