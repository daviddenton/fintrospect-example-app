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
import io.fintrospect.filters.ResponseFilters.ExtractBody
import io.fintrospect.formats.Circe.bodySpec
import io.fintrospect.parameters._
import io.fintrospect.util.{Extracted, ExtractionFailed}

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
    val response = Body.of(bodySpec[User]())
  }

  object Delete {
    val id = Path.of(ParameterSpec.int().as[Id], "id")
    val route = RouteSpec().at(Method.Delete) / "user" / id
  }

  object UserList {
    val route = RouteSpec().at(Get) / "user"
    val response = Body.of(bodySpec[Seq[User]]())
  }

  object Lookup {
    val username = Path.of(ParameterSpec.string().as[Username], "username")
    val route = RouteSpec().at(Get) / "user" / username
    val response = Body.of(bodySpec[User]())
  }

}

/**
  * Remote User Directory service, accessible over HTTP
  */
class UserDirectory(client: Service[Request, Response]) {

  private val createClient = Create.route bindToClient ExtractBody(Create.response).andThen(OnlyAccept(Created)).andThen(client)

  def create(name: Username, inEmail: EmailAddress): Future[User] =
    createClient(Create.form --> Form(Create.username --> name, Create.email --> inEmail)).flatMap {
      case Extracted(Some(user)) => Future(user)
      case _ => Future.exception(RemoteSystemProblem("create", Status.BadGateway))
    }

  private val deleteClient = Delete.route bindToClient OnlyAccept(Ok).andThen(client)

  def delete(id: Id): Future[Unit] = deleteClient(Delete.id --> id).map(_ => ())

  private val listClient = UserList.route bindToClient ExtractBody(UserList.response).andThen(OnlyAccept(Ok)).andThen(client)

  def list(): Future[Seq[User]] = listClient().flatMap {
    case Extracted(users) => Future(users.map(_.toSeq).getOrElse(Seq.empty))
    case ExtractionFailed(_) => Future.exception(RemoteSystemProblem("list", Status.BadGateway))
  }

  private val lookupClient = Lookup.route bindToClient ExtractBody(Lookup.response).andThen(OnlyAccept(Ok, Status.NotFound)).andThen(client)

  def lookup(username: Username): Future[Option[User]] =
    lookupClient(Lookup.username --> username).flatMap {
      case Extracted(r) => Future(r)
      case ExtractionFailed(_) => Future.exception(RemoteSystemProblem("lookup", Status.BadGateway))
    }
}
