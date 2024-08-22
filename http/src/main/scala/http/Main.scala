package geolocation

import cats.effect.*
import cats.syntax.all.*

object Main extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] =
    Resources.make[IO].void
}
