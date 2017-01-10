package example.diagnostic

import java.time.Clock

import com.twitter.finagle.http.path.Root
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{Module, RouteModule}

object Diagnostic {
  def module(clock: Clock): Module = RouteModule(Root / "internal", SimpleJson())
    .withRoute(Ping.route())
    .withRoute(Uptime.route(clock))
}
