package example



import com.twitter.finagle.http.Status.{InternalServerError, ServiceUnavailable}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import io.fintrospect.formats.Circe.ResponseBuilder.implicits._

object CatchAll extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]) =
    service(request)
      .handle {
        case e: RemoteSystemProblem => ServiceUnavailable(e.getMessage)
        case e => InternalServerError(e.getMessage)
      }
}