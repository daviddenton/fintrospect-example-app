package env

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import example._
import example.api.Message
import example.external.UserDirectory
import example.external.UserDirectory.Create
import io.circe.generic.auto._
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.Circe.JsonFormat._
import io.fintrospect.formats.Circe.ResponseBuilder._

import scala.collection.mutable

/**
  * Fake implementation of the User Directory HTTP contract. Note the re-use of the RouteSpecs from UserDirectory.
  */
class FakeUserDirectory extends ServerRoutes[Request, Response] {

  private val users = mutable.Map[Id, User]()

  def contains(newUser: User): Unit = users(newUser.id) = newUser

  def reset() = users.clear()

  add(UserDirectory.Create.route.bindTo(Service.mk[Request, Response] {
    request => {
      val form = UserDirectory.Create.form <-- request
      val (username, email) = form <-- (Create.username, Create.email)
      val newUser = User(Id(users.size), Username(username), EmailAddress(email))
      users(newUser.id) = newUser
      Created(encode(newUser))
    }
  }))

  private def delete(id: Id) = Service.mk[Request, Response] {
    _ =>
      users
        .get(id)
        .map { user => users -= id; Ok(encode(user)).toFuture }
        .getOrElse(NotFound(encode(Message("no such user"))))
  }

  add(UserDirectory.Delete.route.bindTo(delete))

  private def lookup(username: Username) = Service.mk[Request, Response] {
    _: Request =>
      users
        .values
        .find(_.name == username)
        .map { found => Ok(encode(found)).toFuture }
        .getOrElse(NotFound(encode(Message("no such user"))))
  }

  add(UserDirectory.Lookup.route.bindTo(lookup))

  add(UserDirectory.UserList.route.bindTo(Service.mk[Request, Response] { _ => Ok(encode(users.values)) }))

  reset()
}
