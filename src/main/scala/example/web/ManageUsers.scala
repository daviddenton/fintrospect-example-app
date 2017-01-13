package example.web

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Request
import example.external.UserDirectory
import example.{EmailAddress, Id, User, Username}
import io.fintrospect.parameters.StringValidations._
import io.fintrospect.parameters.{Body, Form, FormField, ParameterSpec}
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoute, ServerRoutes}

import scala.language.reflectiveCalls

case class ManageUsersView(users: Seq[User], form: Form) extends View {
  val errors: Seq[String] = form.errors.map(_.reason)
}

object ManageUsers {

  private def viewRoute(userDirectory: UserDirectory): ServerRoute[Request, View] = {
    val service = Service.mk { _: Request => userDirectory.list().map(u => ManageUsersView(u, Form())) }
    RouteSpec().at(Get) / "users" bindTo service
  }

  private def create(userDirectory: UserDirectory): ServerRoute[Request, View] = {
    val username = FormField.required(ParameterSpec.string("username", validation = EmptyIsInvalid).map(Username, (u: Username) => u.value))
    val email = FormField.required(ParameterSpec.string("email", validation = EmptyIsInvalid).map(EmailAddress, (u: EmailAddress) => u.value))
    val form = Body.webForm(username -> "Username is required!", email -> "Email is required!")

    val service: Service[Request, View] = Service.mk {
      request: Request => {
        val formInstance = form <-- request

        if (formInstance.isValid)
          userDirectory.create(username <-- formInstance, email <-- formInstance)
            .flatMap(_ => userDirectory.list())
            .map(_ => View.Redirect("."))
        else userDirectory.list().map(u => ManageUsersView(u, formInstance))
      }
    }
    RouteSpec()
      .body(form)
      .at(Post) / "users" / "create" bindTo service
  }

  private def delete(userDirectory: UserDirectory): ServerRoute[Request, View] = {
    val id = FormField.required(ParameterSpec.int("id").map(Id, (u: Id) => u.value))
    val form = Body.form(id)
    val service = Service.mk {
      request: Request => {
        userDirectory.delete(id <-- (form <-- request))
          .flatMap(_ => userDirectory.list())
          .map(_ => View.Redirect("."))
      }
    }
    RouteSpec()
      .body(form)
      .at(Post) / "users" / "delete" bindTo service
  }

  def routes(userDirectory: UserDirectory): ServerRoutes[Request, View] =
    new ServerRoutes[Request, View] {
      add(viewRoute(userDirectory))
      add(create(userDirectory))
      add(delete(userDirectory))
    }
}
