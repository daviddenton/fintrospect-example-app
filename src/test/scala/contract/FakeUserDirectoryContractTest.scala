package contract

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.InternalServerError
import com.twitter.util.Await
import env.FakeUserDirectory
import example.external.RemoteSystemProblem
import example.{EmailAddress, Username}
import io.fintrospect.testing.OverridableHttpService
import org.scalatest.BeforeAndAfterEach

/**
  * Contract implementation for the Fake.
  */
class FakeUserDirectoryContractTest extends UserDirectoryContract with BeforeAndAfterEach {
  override lazy val username = Username("Bob the Builder")
  override lazy val email = EmailAddress("bob@fintrospect.io")

  lazy val state = new FakeUserDirectory()
  lazy val userServiceHttp = new OverridableHttpService[Response](state)
  override lazy val service = userServiceHttp.service

  override protected def beforeEach(): Unit = state.reset()

  describe("the client responds as expected to failure conditions") {

    describe("list users") {
      it("returns a RemoteException if the response status is not OK") {
        userServiceHttp.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.list())) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("create user") {
      it("returns a RemoteException if the response status is not Created") {
        userServiceHttp.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.create(username, email))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("delete user") {
      it("returns a RemoteException if the response status is not OK") {
        userServiceHttp.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.delete(user.id))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("lookup user") {
      it("returns a RemoteException if the response status is not OK") {
        userServiceHttp.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.lookup(username))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
  }

}
