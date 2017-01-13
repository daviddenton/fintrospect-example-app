package example.api

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Path
import com.twitter.util.Future
import example.Inhabitants
import example.external.{EntryLogger, UserDirectory}
import io.fintrospect.parameters.Header
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{ApiKey, Module, RouteModule}

object Api {
  def module(path: Path, inhabitants: Inhabitants, userDirectory: UserDirectory, entryLogger: EntryLogger): Module = {

    // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic for the API routes
    val corsFilter = new HttpFilter(Cors.UnsafePermissivePolicy)

    val apiKey = ApiKey(Header.required.string("key"), Service.mk { key: String => Future(key.equals("realSecret")) })

    RouteModule(path,
      Swagger2dot0Json(ApiInfo("Security System API", "1.0", "Known users are `Bob`, `Sue`, `Rita`. Security key is `realSecret`")),
      corsFilter
    )
      .withDescriptionPath(_ / "api-docs")
      .securedBy(apiKey)
      .withRoute(KnockKnock.route(inhabitants, userDirectory, entryLogger))
      .withRoute(WhoIsThere.route(inhabitants, userDirectory))
      .withRoute(ByeBye.route(inhabitants, entryLogger))
  }
}
