package simulations

import io.gatling.core.Predef.*
import io.gatling.core.body.StringBody
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import java.nio.charset.Charset
import scala.annotation.nowarn

@nowarn
class GeolocationLoadTest extends Simulation {
  // 1. HTTP Configuration
  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")
    .header("Accept", "application/json")
    .contentTypeHeader("application/json")

  // 2. Scenario Definition
  val newYorkBody =
    """|{
       |  "street": "123 Anywhere St.",
       |  "city": "New York",
       |  "state": "NY"
       |}""".stripMargin
  val scn: ScenarioBuilder = scenario("Address Lookup")
    .exec(
      http("Lookup Address")
        .post("/api/coords")
        .body(StringBody(newYorkBody, Charset.forName("UTF-8"))),
    )

  // 3. Load Scenario
  setUp(
    scn.inject(
      nothingFor(1),
      atOnceUsers(20),
      rampUsers(500).during(5),
      constantUsersPerSec(500).during(15),
    ),
  ).protocols(httpProtocol)
}
