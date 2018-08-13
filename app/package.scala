package lidraughts

import ornicar.scalalib.Zero
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

package object search {

  object Date {
    import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
    val format = "YYYY-MM-dd HH:mm:ss"
    val formatter: DateTimeFormatter = DateTimeFormat forPattern format
  }

  // fix scala

  type Fu[+A] = Future[A]
  type Funit = Fu[Unit]

  def fuccess[A](a: A) = Future successful a
  def fufail[A <: Throwable, B](a: A): Fu[B] = Future failed a
  def fufail[A](a: String): Fu[A] = fufail(new Exception(a))
  val funit = fuccess(())

  implicit final class LidraughtsPimpedFuture[A](fua: Fu[A]) {

    def >>-(sideEffect: => Unit)(implicit ec: ExecutionContext): Fu[A] = fua andThen {
      case _ => sideEffect
    }

    def >>[B](fub: => Fu[B])(implicit ec: ExecutionContext): Fu[B] = fua flatMap (_ => fub)

    def void(implicit ec: ExecutionContext): Funit = fua map (_ => Unit)

    def inject[B](b: => B)(implicit ec: ExecutionContext): Fu[B] = fua map (_ => b)
  }

  implicit class LidraughtsPimpedBoolean(self: Boolean) {

    def fold[A](t: => A, f: => A): A = if (self) t else f

    def option[A](a: => A): Option[A] = if (self) Some(a) else None
  }

  implicit class LidraughtsPimpedOption[A](self: Option[A]) {

    import scalaz.std.{ option => o }

    def |(a: => A): A = self getOrElse a

    def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero
  }
}
