package geolocation.http.routes

import cats.effect.*
import geolocation.services.HelloService
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

object HelloEndpoints {
  def apply[F[_]: Async](helloService: HelloService[F]): List[ServerEndpoint[Any, F]] = List(
    endpoint.post
      .in("api" / "hello")
      .in(query[String]("name"))
      .out(stringBody)
      .serverLogicSuccess[F](helloService.hello),
  )
}
