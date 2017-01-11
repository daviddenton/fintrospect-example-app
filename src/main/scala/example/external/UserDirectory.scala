package example.external

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status.{Created, NotFound, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import example.external.UserDirectory.{Create, Delete, Lookup, UserList}
import example.{EmailAddress, Id, User, Username}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters._

import scala.language.reflectiveCalls

/**
  * Remote User Directory service, accessible over HTTP. We define the Routes making up the HTTP contract here so they can be
  * re-used to provide the Fake implementation which we can dev against.
  */
object UserDirectory {

  object Create {
    val email = FormField.required.string("email")
    val username = FormField.required.string("username")
    val form = Body.form(email, username)
    val route = RouteSpec().body(form).at(Post) / "user"
  }

  object Delete {
    val id = Path(ParameterSpec.int("id").map(Id, (i: Id) => i.value))
    val username = FormField.required.string("username")
    val route = RouteSpec().at(Post) / "user" / id
  }

  object UserList {
    val route = RouteSpec().at(Get) / "user"
  }

  object Lookup {
    val username = Path(ParameterSpec.string("username").map(Username, (u: Username) => u.value.toString))
    val route = RouteSpec().at(Get) / "user" / username
  }

}

/**
  * Remote User Directory service, accessible over HTTP
  */
class UserDirectory(client: Service[Request, Response]) {

  private def expectStatusAndExtract[T](expectedStatus: Status, responseBody: UniBody[T]): Response => Future[T] =
    r => if (r.status == expectedStatus) Future.value(responseBody <-- r)
    else Future.exception(RemoteSystemProblem("user directory", r.status))

  private val createClient = Create.route bindToClient client

  def create(name: Username, inEmail: EmailAddress): Future[User] = {
    val form = Form(Create.username --> name.value, Create.email --> inEmail.value)
    createClient(Create.form --> form)
      .flatMap(expectStatusAndExtract(Created, Body(bodySpec[User]())))
  }

  private val deleteClient = Delete.route bindToClient client

  def delete(user: User): Future[Unit] =
    deleteClient(Delete.id --> user.id)
      .flatMap(
        r => if (r.status == Ok) Future(Unit)
        else Future.exception(RemoteSystemProblem("user directory", r.status)))

  private val listClient = UserList.route bindToClient client

  def list(): Future[Seq[User]] = listClient()
    .flatMap(expectStatusAndExtract(Ok, Body(bodySpec[Seq[User]]())))

  private val lookupClient = Lookup.route bindToClient client

  def lookup(username: Username): Future[Option[User]] =
    lookupClient(Lookup.username --> username)
      .flatMap(response =>
        response.status match {
          case Ok => Future.value(Some(Body(bodySpec[User]()) <-- response))
          case NotFound => Future(None)
          case _ => Future.exception(RemoteSystemProblem("user directory", response.status))
        }
      )
}
