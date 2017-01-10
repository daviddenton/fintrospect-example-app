import java.time.Clock


package object example {
  type Events = (Event => Unit)

  case class Event(description: String)

  case class Id(value: Int)

  case class Username(value: String)

  case class EmailAddress(value: String)

  case class User(id: Id, name: Username, email: EmailAddress)

  case class UserEntry(username: String, goingIn: Boolean, timestamp: Long)

  object UserEntry {
    def entering(username: Username, clock: Clock) = UserEntry(username.value, goingIn = true, clock.instant().toEpochMilli)

    def exiting(username: Username, clock: Clock) = UserEntry(username.value, goingIn = false, clock.instant().toEpochMilli)
  }
}

