package geolocation.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.Signal
import geolocation.domain.*
import geolocation.repositories.codecs.*
import skunk.*
import skunk.data.*
import skunk.net.protocol.Describe
import skunk.net.protocol.Parse
import skunk.util.Origin
import skunk.util.Typer

object Stubs {
  val emptyConfig = Config(
    port = 5432,
    databaseConfig = DatabaseConfig(
      host = "",
      port = 0,
      username = "",
      password = "",
      database = "",
      maxConnections = 0,
      migrationsLocation = "",
    ),
  )

  class StubSession[F[_]: Sync] extends Session[F] {
    override def parameters: Signal[F, Map[String, String]]                                                                                = ???
    override def parameter(key: String): Stream[F, String]                                                                                 = ???
    override def transactionStatus: Signal[F, TransactionStatus]                                                                           = ???
    override def execute[A](query: Query[Void, A]): F[List[A]]                                                                             = ???
    override def execute[A, B](query: Query[A, B])(args: A): F[List[B]]                                                                    = ???
    override def execute[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[List[B]]                                        = ???
    override def unique[A](query: Query[Void, A]): F[A]                                                                                    = ???
    override def unique[A, B](query: Query[A, B])(args: A): F[B]                                                                           = ???
    override def unique[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[B]                                               = ???
    override def option[A](query: Query[Void, A]): F[Option[A]]                                                                            = ???
    override def option[A, B](query: Query[A, B])(args: A): F[Option[B]]                                                                   = ???
    override def option[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[Option[B]]                                       = ???
    override def stream[A, B](command: Query[A, B])(args: A, chunkSize: Int): Stream[F, B]                                                 = ???
    override def stream[A, B](query: Query[A, B], args: A, chunkSize: Int)(implicit ev: DummyImplicit): Stream[F, B]                       = ???
    override def cursor[A, B](query: Query[A, B])(args: A): Resource[F, Cursor[F, B]]                                                      = ???
    override def cursor[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): Resource[F, Cursor[F, B]]                          = ???
    override def execute(command: Command[Void]): F[Completion]                                                                            = (Completion.Insert(1)).pure
    override def execute[A](command: Command[A])(args: A): F[Completion]                                                                   = ???
    override def execute[A](command: Command[A], args: A)(implicit ev: DummyImplicit): F[Completion]                                       = ???
    override def executeDiscard(statement: Statement[Void]): F[Unit]                                                                       = ???
    override def prepare[A, B](query: Query[A, B]): F[PreparedQuery[F, A, B]]                                                              = (new StubPreparedQuery).pure
    override def prepare[A](command: Command[A]): F[PreparedCommand[F, A]]                                                                 = (new StubPreparedCommand).pure
    override def prepareR[A, B](query: Query[A, B]): Resource[F, PreparedQuery[F, A, B]]                                                   = ???
    override def prepareR[A](command: Command[A]): Resource[F, PreparedCommand[F, A]]                                                      = ???
    override def pipe[A](command: Command[A]): Pipe[F, A, Completion]                                                                      = ???
    override def pipe[A, B](query: Query[A, B], chunkSize: Int): Pipe[F, A, B]                                                             = ???
    override def channel(name: Identifier): Channel[F, String, String]                                                                     = ???
    override def transaction[A]: Resource[F, Transaction[F]]                                                                               = ???
    override def transaction[A](isolationLevel: TransactionIsolationLevel, accessMode: TransactionAccessMode): Resource[F, Transaction[F]] = ???
    override def typer: Typer                                                                                                              = ???
    override def describeCache: Describe.Cache[F]                                                                                          = ???
    override def parseCache: Parse.Cache[F]                                                                                                = ???
  }

  class StubPreparedQuery[F[_], A, B] extends PreparedQuery[F, A, B] {
    override def cursor(args: A)(implicit or: Origin): Resource[F, Cursor[F, B]] = ???
    override def stream(args: A, chunkSize: Int)(implicit or: Origin): Stream[F, B] = Stream(
      AddressRow(
        id = 1,
        street = "123 Anywhere St.",
        city = "New York",
        state = "NY",
        lat = 1,
        lon = 1,
      ).asInstanceOf[B],
    )
    override def option(args: A)(implicit or: Origin): F[Option[B]]       = ???
    override def unique(args: A)(implicit or: Origin): F[B]               = ???
    override def pipe(chunkSize: Int)(implicit or: Origin): Pipe[F, A, B] = ???
  }

  class StubPreparedCommand[F[_]: Applicative, A] extends PreparedCommand[F, A] {
    override def execute(args: A)(implicit origin: Origin): F[Completion] = Completion.Insert(1).pure
    override def pipe(implicit origin: Origin): Pipe[F, A, Completion]    = ???
    override def mapK[G[_]](fk: F ~> G): PreparedCommand[G, A]            = ???
  }

  class StubFailingSession[F[_]: Sync] extends Session[F] {
    override def parameters: Signal[F, Map[String, String]]                                                                                = ???
    override def parameter(key: String): Stream[F, String]                                                                                 = ???
    override def transactionStatus: Signal[F, TransactionStatus]                                                                           = ???
    override def execute[A](query: Query[Void, A]): F[List[A]]                                                                             = ???
    override def execute[A, B](query: Query[A, B])(args: A): F[List[B]]                                                                    = ???
    override def execute[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[List[B]]                                        = ???
    override def unique[A](query: Query[Void, A]): F[A]                                                                                    = ???
    override def unique[A, B](query: Query[A, B])(args: A): F[B]                                                                           = ???
    override def unique[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[B]                                               = ???
    override def option[A](query: Query[Void, A]): F[Option[A]]                                                                            = ???
    override def option[A, B](query: Query[A, B])(args: A): F[Option[B]]                                                                   = ???
    override def option[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): F[Option[B]]                                       = ???
    override def stream[A, B](command: Query[A, B])(args: A, chunkSize: Int): Stream[F, B]                                                 = ???
    override def stream[A, B](query: Query[A, B], args: A, chunkSize: Int)(implicit ev: DummyImplicit): Stream[F, B]                       = ???
    override def cursor[A, B](query: Query[A, B])(args: A): Resource[F, Cursor[F, B]]                                                      = ???
    override def cursor[A, B](query: Query[A, B], args: A)(implicit ev: DummyImplicit): Resource[F, Cursor[F, B]]                          = ???
    override def execute(command: Command[Void]): F[Completion]                                                                            = ???
    override def execute[A](command: Command[A])(args: A): F[Completion]                                                                   = ???
    override def execute[A](command: Command[A], args: A)(implicit ev: DummyImplicit): F[Completion]                                       = ???
    override def executeDiscard(statement: Statement[Void]): F[Unit]                                                                       = ???
    override def prepare[A, B](query: Query[A, B]): F[PreparedQuery[F, A, B]]                                                              = ???
    override def prepare[A](command: Command[A]): F[PreparedCommand[F, A]]                                                                 = Sync[F].raiseError(Throwable("BANG!"))
    override def prepareR[A, B](query: Query[A, B]): Resource[F, PreparedQuery[F, A, B]]                                                   = ???
    override def prepareR[A](command: Command[A]): Resource[F, PreparedCommand[F, A]]                                                      = ???
    override def pipe[A](command: Command[A]): Pipe[F, A, Completion]                                                                      = ???
    override def pipe[A, B](query: Query[A, B], chunkSize: Int): Pipe[F, A, B]                                                             = ???
    override def channel(name: Identifier): Channel[F, String, String]                                                                     = ???
    override def transaction[A]: Resource[F, Transaction[F]]                                                                               = ???
    override def transaction[A](isolationLevel: TransactionIsolationLevel, accessMode: TransactionAccessMode): Resource[F, Transaction[F]] = ???
    override def typer: Typer                                                                                                              = ???
    override def describeCache: Describe.Cache[F]                                                                                          = ???
    override def parseCache: Parse.Cache[F]                                                                                                = ???
  }
}
