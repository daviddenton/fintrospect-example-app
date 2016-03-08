package contract

import com.twitter.finagle.Http
import org.scalatest.Ignore

/**
  * Contract implementation for the real Entry Logger service. Extra steps might be required here to setup/teardown
  * test data.
  */
@Ignore // this would not be ignored in reality
class RealEntryLoggerContractTest extends EntryLoggerContract {

  // real test data would be set up here for the required environment
  override lazy val service = Http.newService("google.com:80")
}
