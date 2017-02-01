package env

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import example.UserEntry
import example.external.EntryLogger.{Entry, Exit, LogList}
import io.circe.generic.auto._
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder._

import scala.collection.mutable

/**
  * Fake implementation of the Entry Logger HTTP contract. Note the re-use of the RouteSpecs from EntryLogger.
  */
class FakeEntryLogger extends ServerRoutes[Request, Response] {

  val entries = mutable.MutableList[UserEntry]()

  def reset() = entries.clear()

  add(Entry.route.bindTo(
    Service.mk[Request, Response] { request =>
      val userEntry = Entry.body <-- request
      entries += userEntry
      Created(encode(userEntry))
    })
  )

  add(
    Exit.route.bindTo(
      Service.mk[Request, Response] { request =>
        val userEntry = Exit.body <-- request
        entries += userEntry
        Created(encode(userEntry))
      })
  )

  add(LogList.route.bindTo(Service.mk[Request, Response] { _ => Ok(encode(entries)) }))

  reset()
}
