package example.external

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

object OnlyAccept {
  def apply(statuses: Status*): Filter[Request, Response, Request, Response] =
    Filter.mk {
      (req, svc) =>
        svc(req).flatMap {
          case ok if statuses.contains(ok.status) => Future(ok)
          case notOk => Future.exception(RemoteSystemProblem(req.uri, notOk.status))
        }
    }
}
