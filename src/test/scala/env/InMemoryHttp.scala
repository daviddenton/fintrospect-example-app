package env

import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.{ModuleSpec, ServerRoutes}

class InMemoryHttp(serverRoutes: ServerRoutes[Response]) {

  private var overrideStatus = Option.empty[Status]

  private val possibleError = new SimpleFilter[Request, Response] {
    override def apply(request: Request, service: Service[Request, Response]) = overrideStatus
      .map(s => Future.value(Response(s)))
      .getOrElse(service(request))
  }

  /**
    * Override the status code returned by the server
    */
  def respondWith(status: Status) = overrideStatus = if (status == Status.Ok) None else Option(status)

  val service = possibleError.andThen(ModuleSpec(Root).withRoutes(serverRoutes).toService)
}
