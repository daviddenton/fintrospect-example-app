package env

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import example.EntryLogger._
import example._
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._

/**
  * Fake implementation of the Entry Logger HTTP contract. Note the re-use of the RouteSpecs from EntryLogger.
  */
class FakeEntryLogger extends ServerRoutes[Response] {

  var entries: Seq[UserEntry] = null

  def reset() = entries = Seq[UserEntry]()

  add(Entry.route.bindTo(Service.mk[Request, Response] { request =>
    val userEntry = Entry.entry <-- request
    entries = entries :+ userEntry
    Created(encode(userEntry))
  })
  )

  add(
    Exit.route.bindTo(Service.mk[Request, Response] { request =>
      val userEntry = Exit.entry <-- request
      entries = entries :+ userEntry
      Created(encode(userEntry))
    })
  )

  add(LogList.route.bindTo(Service.mk[Request, Response] { _ => Ok(encode(entries)) }))

  reset()
}
