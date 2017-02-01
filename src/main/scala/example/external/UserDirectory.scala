package example.external

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import example.external.UserDirectory.{Create, Delete, Lookup, UserList}
import example.{EmailAddress, Id, User, Username}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters._
import io.fintrospect.util.{Extracted, ExtractionFailed}

import scala.language.reflectiveCalls

/**
  * Remote User Directory service, accessible over HTTP. We define the Routes making up the HTTP contract here so they can be
  * re-used to provide the Fake implementation which we can dev against.
  */
object UserDirectory {

  object Create {
    val email = FormField.required(ParameterSpec.string().as[EmailAddress], "email")
    val username = FormField.required(ParameterSpec.string().as[Username], "username")
    val form = Body.form(email, username)
    val route = RouteSpec().body(form).at(Post) / "user"
    val response = Body(bodySpec[User]())
  }

  object Delete {
    val id = Path(ParameterSpec.int().as[Id], "id")
    val route = RouteSpec().at(Method.Delete) / "user" / id
  }

  object UserList {
    val route = RouteSpec().at(Get) / "user"
    val response = Body(bodySpec[Seq[User]]())
  }

  object Lookup {
    val username = Path(ParameterSpec.string().as[Username], "username")
    val route = RouteSpec().at(Get) / "user" / username
    val response = Body(bodySpec[User]())
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
    val form = Form(Create.username --> name, Create.email --> inEmail)
    createClient(Create.form --> form)
      .flatMap(expectStatusAndExtract(Created, Create.response))
  }

  private val deleteClient = Delete.route bindToClient client

  def delete(id: Id): Future[Unit] =
    deleteClient(Delete.id --> id)
      .flatMap(
        r => if (r.status == Ok) Future(Unit)
        else Future.exception(RemoteSystemProblem("user directory", r.status)))

  private val listClient = UserList.route bindToClient client

  def list(): Future[Seq[User]] = listClient()
    .flatMap(expectStatusAndExtract(Ok, UserList.response))

  private val lookupClient = Lookup.route bindToClient client

  def lookup(username: Username): Future[Option[User]] =
    lookupClient(Lookup.username --> username)
      .flatMap(response =>
        Lookup.response <--? response match {
          case Extracted(r) => Future.value(r)
          case ExtractionFailed(e) => Future.exception(RemoteSystemProblem("user directory", response.status))
        }
      )
}
