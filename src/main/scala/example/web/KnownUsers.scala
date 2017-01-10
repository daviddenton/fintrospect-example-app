package example.web

import example.User
import io.fintrospect.templating.View

case class KnownUsers(users: Seq[User]) extends View
