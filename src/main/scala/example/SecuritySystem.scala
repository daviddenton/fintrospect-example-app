package example

import java.net.URL
import java.time.Clock

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.Module.combine
import io.fintrospect.ResourceLoader.Classpath
import io.fintrospect.formats.Html
import io.fintrospect.parameters.Header
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.templating.{MustacheTemplates, RenderView}
import io.fintrospect.{ApiKey, RouteModule, StaticModule}

/**
  * Sets up the business-level API for the application. Note that the generic clients on the constructor allow us to
  * inject non-HTTP versions of the downstream dependencies so we can run tests without starting up real HTTP servers.
  */
class SecuritySystem(userDirectoryHttp: Service[Request, Response], entryLoggerHttp: Service[Request, Response], clock: Clock) {

  private val userDirectory = new UserDirectory(userDirectoryHttp)
  private val entryLogger = new EntryLogger(entryLoggerHttp, clock)
  private val inhabitants = new Inhabitants

  private val apiKey = ApiKey(Header.required.string("key"), Service.mk { key: String => Future.value(key.equals("realSecret")) })

  private val serviceModule = RouteModule(Root / "security",
    Swagger2dot0Json(ApiInfo("Security System API", "1.0", "Known users are `Bob`, `Sue`, `Rita`. Security key is `realSecret`")),
    new RequestCountingFilter(System.out)
  )
    .withDescriptionPath(_ / "api-docs")
    .securedBy(apiKey)
    .withRoute(new KnockKnock(inhabitants, userDirectory, entryLogger).route)
    .withRoute(new WhoIsThere(inhabitants, userDirectory).route)
    .withRoute(new ByeBye(inhabitants, entryLogger).route)

  private val internalModule = RouteModule(Root / "internal", SimpleJson()).withRoute(new Ping().route)

  private val webModule = RouteModule(Root,
    new SiteMapModuleRenderer(new URL("http://my.security.system")),
    new RenderView(Html.ResponseBuilder, MustacheTemplates.CachingClasspath("templates"))
  )
    .withDescriptionPath(_ / "sitemap.xml")
    .withRoute(new ShowKnownUsers(userDirectory).route)
    .withRoute(new ShowIndex(userDirectory).route)

  private val publicModule = StaticModule(Root, Classpath("public"))

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the system
  private val globalFilter = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(CatchAll)

  val service: Service[Request, Response] = globalFilter.andThen(
    combine(serviceModule, internalModule, publicModule, webModule).toService
  )
}
