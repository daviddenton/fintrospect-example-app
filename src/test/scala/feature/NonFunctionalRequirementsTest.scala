package feature

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import env.RunningTestEnvironment
import io.fintrospect.ContentTypes
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import org.json4s.JString
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.xml.Utility.trim
import scala.xml.{Utility, XML}

class NonFunctionalRequirementsTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  it("responds to ping") {
    val response = env.responseTo(Request("/internal/ping"))
    response.status shouldBe Ok
    response.content shouldBe "pong"
  }

  it("serves static content") {
    val response = env.responseTo(Request("/style.css"))
    response.status shouldBe Ok
    response.content shouldBe "body { font-family: \"Droid Sans\", sans-serif; } .content { padding: 50px; }"
  }

  it("has a sitemap") {
    val response = env.responseTo(Request("/sitemap.xml"))
    response.status shouldBe Ok
    response.contentType.startsWith(ContentTypes.APPLICATION_XML.value) shouldBe true
    val siteMap = trim(XML.loadString(response.content))
    ((siteMap \\ "urlset" \\ "url")(0) \\ "loc").text shouldBe "http://my.security.system/known"
    ((siteMap \\ "urlset" \\ "url")(1) \\ "loc").text shouldBe "http://my.security.system"
  }

  it("provides API documentation in swagger 2.0 format") {
    val response = env.responseTo(Request("/security/api-docs"))
    response.status shouldBe Ok

    JsonFormat.parse(response.content).children.head.asInstanceOf[JString].values shouldBe "2.0"
  }
}
