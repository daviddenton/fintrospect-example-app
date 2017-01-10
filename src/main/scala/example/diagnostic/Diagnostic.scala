package example.diagnostic

import com.twitter.finagle.http.path.Root
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{Module, RouteModule}

object Diagnostic {
  def module(): Module = RouteModule(Root / "internal", SimpleJson()).withRoute(Ping.route())
}
