import io.fintrospect.parameters.Value


package object example {
  type Events = (Event => Unit)

  case class Event(description: String)

  case class Id(value: Int) extends AnyVal with Value[Int]

  case class Username(value: String) extends AnyVal with Value[String]

  case class EmailAddress(value: String) extends AnyVal with Value[String]

  case class User(id: Id, name: Username, email: EmailAddress)

  case class UserEntry(username: String, goingIn: Boolean, timestamp: Long)
}

