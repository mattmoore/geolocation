package geolocation.http.metrics

import cats.effect.Async
import io.prometheus.metrics.core.metrics.Counter
import sttp.monad.MonadError
import sttp.tapir.Endpoint
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.metrics.EndpointMetric
import sttp.tapir.server.metrics.Metric
import sttp.tapir.server.model.ServerResponse

type OnResponseBody[F[_]] = (
    ep: Endpoint[?, ?, ?, ?, ?],
    req: ServerRequest,
    res: ServerResponse[?],
    counter: Counter,
) => F[Unit]

case class CustomMetricConfig[F[_]: Async](
    prefix: Option[String] = None,
    counter: Counter,
    endpoint: Option[Endpoint[?, ?, ?, ?, ?]] = None,
    onResponseBody: OnResponseBody[F],
) {
  def tapirMetric: Metric[F, Counter] =
    Metric[F, Counter](
      counter,
      onRequest = { (req: ServerRequest, counter: Counter, me: MonadError[F]) =>
        Async[F].delay {
          EndpointMetric(
            onResponseBody = Option({ (ep, res) =>
              val endpointName = endpoint.flatMap(_.info.name)
              ep.info.name match {
                case `endpointName` => onResponseBody(ep, req, res, counter)
                case _              => Async[F].unit
              }
            }),
          )
        }
      },
    )

  // private def prefixedCounterName(counter: Counter): Counter = {
  //   val name = prefix match {
  //     case Some(p) => s"${p}_${counter.getPrometheusName()}"
  //     case None => counter.getPrometheusName()
  //   }
  //   counter
  // }
}
