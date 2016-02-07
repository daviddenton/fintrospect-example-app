package feature

import com.twitter.finagle.http.Method.{Post, Get}
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import env.{ResponseStatusAndContent, RunningTestEnvironment}
import example._
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import org.scalatest.{FunSpec, ShouldMatchers}

class ReportInhabitantsTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  describe("whoIsThere endpoint validation") {
    it("is protected with a secret key") {
      checkInhabitants("fakeSecret").status shouldBe Unauthorized
    }
  }

  describe("proper usage") {
    it("initially there is no-one inside") {
      val inhabitants = checkInhabitants("realSecret")
      inhabitants.status shouldBe Ok
      decode[Seq[User]](parse(inhabitants.content)) shouldBe Seq()
    }

    it("when a user enters the building") {
      val user = User(Id(1), Username("Bob"), EmailAddress("bob@bob.com"))

      env.userDirectory.contains(user)

      enterBuilding(Option("Bob"), "realSecret")
      val inhabitants = checkInhabitants("realSecret")
      inhabitants.status shouldBe Ok
      decode[Seq[User]](parse(inhabitants.content)) shouldBe Seq(user)
    }
  }

  private def enterBuilding(user: Option[String], secret: String): ResponseStatusAndContent = {
    val query = user.map("username=" + _).getOrElse("")
    val request = Request(Post, "/security/knock?" + query)
    request.headerMap("key") = secret
    env.responseTo(request)
  }

  private def checkInhabitants(secret: String): ResponseStatusAndContent = {
    val request = Request(Get, "/security/whoIsThere")
    request.headerMap("key") = secret
    env.responseTo(request)
  }
}
