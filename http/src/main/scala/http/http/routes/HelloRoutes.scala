package geolocation.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import geolocation.http.*
import geolocation.services.*
import org.http4s.*
import org.http4s.dsl.*

object HelloRoutes {
  def apply[F[_]: Async](
      dsl: Http4sDsl[F],
      helloService: HelloService[F],
  ): HttpRoutes[F] = {
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        helloService
          .hello(name)
          .flatMap(Ok(_))
          .handleErrorWith(e => InternalServerError(e.getMessage))
      case GET -> Root / "healthcheck" =>
        Ok()
    }
  }
}
