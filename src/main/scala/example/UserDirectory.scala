package example


import java.lang.Integer.parseInt

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status.{Created, NotFound, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import example.UserDirectory.{Lookup, Delete, Create, UserList}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.json.Json4s.Native.JsonFormat.bodySpec
import io.fintrospect.parameters.{Body, Form, FormField, NumberParamType, ParameterSpec, Path, StringParamType}

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
    val id = Path(ParameterSpec[Id]("id", None, NumberParamType, s => Id(parseInt(s)), _.value.toString))
    val username = FormField.required.string("username")
    val route = RouteSpec().at(Post) / "user" / id
  }

  object UserList {
    val route = RouteSpec().at(Get) / "user"
  }

  object Lookup {
    val username = Path(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))
    val route = RouteSpec().at(Get) / "user" / username
  }

}

/**
  * Remote User Directory service, accessible over HTTP
  */
class UserDirectory(client: Service[Request, Response]) {

  private def expectStatusAndExtract[T](expectedStatus: Status, responseBody: Body[T]): Response => Future[T] =
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
        r => if (r.status == Ok) Future.value(Unit)
        else Future.exception(RemoteSystemProblem("user directory", r.status)))

  private val listClient = UserList.route bindToClient client

  def list(): Future[Seq[User]] = listClient()
    .flatMap(expectStatusAndExtract(Ok, Body(bodySpec[Seq[User]]())))

  private val lookupClient = Lookup.route bindToClient client

  def lookup(username: Username): Future[Option[User]] =
    lookupClient(Lookup.username --> username)
      .flatMap {
        r => r.status match {
          case Ok => Future.value(Some(Body(bodySpec[User]()) <-- r))
          case NotFound => Future.value(None)
          case s => Future.exception(RemoteSystemProblem("user directory", r.status))
        }
      }
}
