package example.diagnostic

import java.time.Clock

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import example.{Event, Events}

/**
  * Filter used to track and report number of successful requests to the service
  */
object Auditor {
  def apply(clock: Clock, events: Events) = new SimpleFilter[Request, Response] {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] =
      service(request).onSuccess {
        response => events(Event(s"${clock.instant().toString}: uri=${request.method}:${request.uri} status=${response.status.code}"))
      }
  }
}
