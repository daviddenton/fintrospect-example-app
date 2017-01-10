package example.api

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.util.Future
import example.diagnostic.RequestCountingFilter
import example.external.{EntryLogger, UserDirectory}
import example.{Events, Inhabitants}
import io.fintrospect.parameters.Header
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{ApiKey, Module, RouteModule}

object Api {
  def module(inhabitants: Inhabitants, userDirectory: UserDirectory, entryLogger: EntryLogger, events: Events): Module = {

    // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic for the API routes
    val corsFilter = new HttpFilter(Cors.UnsafePermissivePolicy)

    val apiKey = ApiKey(Header.required.string("key"), Service.mk { key: String => Future.value(key.equals("realSecret")) })

    RouteModule(Root / "security",
      Swagger2dot0Json(ApiInfo("Security System API", "1.0", "Known users are `Bob`, `Sue`, `Rita`. Security key is `realSecret`")),
      corsFilter.andThen(new RequestCountingFilter(events))
    )
      .withDescriptionPath(_ / "api-docs")
      .securedBy(apiKey)
      .withRoute(KnockKnock.route(inhabitants, userDirectory, entryLogger))
      .withRoute(WhoIsThere.route(inhabitants, userDirectory))
      .withRoute(ByeBye.route(inhabitants, entryLogger))
  }
}
