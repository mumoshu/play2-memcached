import org.specs2.mutable.Specification
import org.specs2.specification.{Step, Fragments}
import play.api.test.{FakeApplication, TestServer}

/**
 * @author KUOKA Yusuke
 */

trait StartStop {

  val additionalConfiguration: Map[String, String]

  lazy val testServer: TestServer = TestServer(3333, FakeApplication(additionalConfiguration = additionalConfiguration))

  def startServer {
    testServer.start()
  }

  def stopServer {
    testServer.stop()
  }
}

trait ServerIntegrationSpec extends Specification with StartStop {

  override def map(fs: => Fragments) = Step(startServer) ^ fs ^ Step(stopServer)
}
