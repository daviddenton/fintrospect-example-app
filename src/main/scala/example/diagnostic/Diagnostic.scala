package example.diagnostic

import java.time.Clock

import com.twitter.finagle.http.path.Path
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{Module, RouteModule}

object Diagnostic {
  def module(path: Path, clock: Clock): Module = RouteModule(path, SimpleJson())
    .withRoute(Ping.route())
    .withRoute(Uptime.route(clock))
}
