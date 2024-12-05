package geolocation.http.routes

import cats.effect.*
import io.prometheus.metrics.core.metrics.Counter
import sttp.tapir.*
import sttp.tapir.server.metrics.Metric

object EndpointImplicits {
  implicit class EndpointExtensions[A, B, C, D, E](endpoint: Endpoint[A, B, C, D, E]) {
    def metric[F[_]: Async](m: => Metric[F, Counter]): Endpoint[A, B, C, D, E] =
      endpoint.attribute(AttributeKey[Metric[F, Counter]], m)
  }
}
