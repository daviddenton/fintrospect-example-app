package example.external

import com.twitter.finagle.http.Status

/**
  * Represents a problem talking to a downstream system
  */
case class RemoteSystemProblem(name: String, status: Status) extends Exception(s"$name returned $status")