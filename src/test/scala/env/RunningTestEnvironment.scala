package env

import org.scalatest.{BeforeAndAfterEach, Suite}

trait RunningTestEnvironment extends BeforeAndAfterEach {
  self: Suite =>

  var env: TestEnvironment

  override protected def beforeEach() = {
    env = new TestEnvironment()
  }
}
