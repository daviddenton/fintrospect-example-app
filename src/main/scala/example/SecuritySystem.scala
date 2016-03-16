package example

import java.net.URL
import java.time.Clock

import com.twitter.finagle._
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Html
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.templating.{RenderMustacheView, View}
import io.fintrospect.{Module, ModuleSpec, StaticModule}

/**
  * Sets up the business-level API for the application. Note that the generic clients on the constructor allow us to
  * inject non-HTTP versions of the downstream dependencies so we can run tests without starting up real HTTP servers.
  */
class SecuritySystem(userDirectoryClient: Service[Request, Response], entryLoggerClient: Service[Request, Response], clock: Clock) {

  private val userDirectory = new UserDirectory(userDirectoryClient)
  private val entryLogger = new EntryLogger(entryLoggerClient, clock)
  private val inhabitants = new Inhabitants

  private val serviceModule = ModuleSpec(Root / "security",
    Swagger2dot0Json(ApiInfo("Security System API", "1.0", Option("Known users are `Bob`, `Sue`, `Rita`. Security key is `realSecret`"))),
    new RequestCountingFilter(System.out)
  )
    .withDescriptionPath(_ / "api-docs")
    .securedBy(SecuritySystemAuth())
    .withRoutes(new KnockKnock(inhabitants, userDirectory, entryLogger))
    .withRoutes(new WhoIsThere(inhabitants, userDirectory))
    .withRoutes(new ByeBye(inhabitants, entryLogger))

  private val internalModule = ModuleSpec(Root / "internal", SimpleJson()).withRoute(new Ping().route)

  private val webModule = ModuleSpec(Root,
    new SiteMapModuleRenderer(new URL("http://my.security.system")),
    new RenderMustacheView(Html.ResponseBuilder, "templates")
  )
    .withDescriptionPath(_ / "sitemap.xml")
    .withRoutes(new ShowKnownUsers(userDirectory))
    .withRoutes(new ShowIndex(userDirectory))

  private val publicModule = StaticModule(Root, "public")

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the system
  private val globalFilter = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(CatchAll)

  val service = globalFilter.andThen(
    Module.toService(Module.combine(serviceModule, internalModule, publicModule, webModule))
  )
}
