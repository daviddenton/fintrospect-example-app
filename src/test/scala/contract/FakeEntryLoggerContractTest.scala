package contract

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.{InternalServerError, NotFound}
import com.twitter.util.Await
import env.FakeEntryLogger
import example.Username
import example.external.RemoteSystemProblem
import io.fintrospect.testing.OverridableHttpService
import org.scalatest.BeforeAndAfterEach

/**
  * Contract implementation for the Fake. We also test failure modes of our client here..
  */
class FakeEntryLoggerContractTest extends EntryLoggerContract with BeforeAndAfterEach {
  lazy val state = new FakeEntryLogger()
  lazy val entryLoggerHttp = new OverridableHttpService[Response](state)
  override lazy val service = entryLoggerHttp.service

  override protected def beforeEach(): Unit = state.reset()

  describe("the client responds as expected to failure conditions") {
    describe("log list") {
      it("returns a RemoteException if the response status is not Created") {
        entryLoggerHttp.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(entryLogger.list())) shouldBe RemoteSystemProblem("/list", InternalServerError)
      }
    }
    describe("log entry") {
      it("returns a RemoteException if the response status is not OK") {
        entryLoggerHttp.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.enter(Username("bob")))) shouldBe RemoteSystemProblem("/entry", NotFound)
      }
    }
    describe("log exit") {
      it("returns a RemoteException if the response status is not OK") {
        entryLoggerHttp.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.exit(Username("bob")))) shouldBe RemoteSystemProblem("/exit", NotFound)
      }
    }
  }

}
