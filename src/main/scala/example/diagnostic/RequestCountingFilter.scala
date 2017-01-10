package example.diagnostic

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import example.{Event, Events}

/**
  * Filter used to track and report number of successful requests to the service
  */
class RequestCountingFilter(events: Events) extends SimpleFilter[Request, Response] {
  private var successes = 0

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
      .onSuccess(_ => {
        successes += 1
        if (successes % 10 == 0) {
          events(Event(s"$successes successful requests served!"))
        }
      })
  }
}
