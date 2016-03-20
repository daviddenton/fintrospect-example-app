package contract

import java.time.{Clock, Instant, ZoneId}

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Request}
import com.twitter.util.Await
import example.{UserEntry, Username, EntryLogger}
import org.scalatest.{FunSpec, ShouldMatchers}

/**
 * This represents the contract that both the real and fake EntryLogger servers will adhere to.
 */
trait EntryLoggerContract extends FunSpec with ShouldMatchers {
  def service: Service[Request, Response]

  private val time = Instant.now()
  val entryLogger = new EntryLogger(service, Clock.fixed(time, ZoneId.systemDefault()))

  it("can log a user entry and it is listed") {
    Await.result(entryLogger.enter(Username("bob"))) shouldBe UserEntry("bob", goingIn = true, time.toEpochMilli)
    Await.result(entryLogger.exit(Username("bob"))) shouldBe UserEntry("bob", goingIn = false, time.toEpochMilli)

    Await.result(entryLogger.list()) shouldBe Seq(
      UserEntry("bob", goingIn = true, time.toEpochMilli),
      UserEntry("bob", goingIn = false, time.toEpochMilli)
    )
  }
}


