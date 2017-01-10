package env


import example._
import io.fintrospect.configuration.{Host, Port}
import io.fintrospect.testing.TestHttpServer

object RunnableEnvironment extends App {
  val serverPort = 9999
  val userDirectoryPort = 10000
  val entryLoggerPort = 10001

  val userDirectory = new FakeUserDirectory()
  userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
  userDirectory.contains(User(Id(2), Username("Rita"), EmailAddress("rita@bob.com")))
  userDirectory.contains(User(Id(3), Username("Sue"), EmailAddress("sue@bob.com")))

  new TestHttpServer(userDirectoryPort, userDirectory).start()
  new TestHttpServer(entryLoggerPort, new FakeEntryLogger()).start()

  new SecuritySystemServer(Port(serverPort),
    Host.localhost.toAuthority(Port(userDirectoryPort)),
    Host.localhost.toAuthority(Port(entryLoggerPort))
  ).start()

  Thread.currentThread().join()
}
