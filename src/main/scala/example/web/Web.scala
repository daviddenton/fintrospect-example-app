package example.web

import java.net.URL

import com.twitter.finagle.http.path.{Path, Root}
import example.external.UserDirectory
import io.fintrospect.ResourceLoader.Classpath
import io.fintrospect.formats.Html
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.templating.{MustacheTemplates, RenderView}
import io.fintrospect.{Module, RouteModule, StaticModule}

object Web {
  def module(path: Path, userDirectory: UserDirectory): Module = {
    val webModule = RouteModule(path,
      new SiteMapModuleRenderer(new URL("http://my.security.system")),
      new RenderView(Html.ResponseBuilder, MustacheTemplates.CachingClasspath("templates"))
    )
      .withDescriptionPath(_ / "sitemap.xml")
      .withRoutes(ManageUsers.routes(userDirectory))
      .withRoute(ShowIndex.route(userDirectory))

    StaticModule(Root, Classpath("public")).combine(webModule)
  }
}
