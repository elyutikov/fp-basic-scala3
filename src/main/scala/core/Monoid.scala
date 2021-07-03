package core

trait Monoid[A] extends Semigroup[A]:
  def combine(x: A, y: A): A
  def empty: A
  extension (x: A)
    infix def |+|(y: A): A = combine(x, y)

object Monoid:
  given Monoid[String] with
    def combine(x: String, y: String): String = x + y
    def empty: String = ""

  given Monoid[Int] with
    def combine(x: Int, y: Int): Int = x + y
    def empty: Int = 0

  given Monoid[Boolean] with
    def combine(x: Boolean, y: Boolean): Boolean = x && y
    def empty: Boolean = true

  given [A]: Monoid[List[A]] with
    def combine(x: List[A], y: List[A]): List[A] = x ::: y
    def empty: List[A] = Nil

  given [A, B](using A: Monoid[A], B: Monoid[B]): Monoid[(A, B)] with
    def combine(x: (A, B), y: (A, B)): (A, B) = (A.combine(x._1, y._1), B.combine(x._2, y._2))
    def empty: (A, B) = (A.empty, B.empty)

  given [A : Monoid]: Monoid[Option[A]] with
    def empty: Option[A] = None
    def combine(x: Option[A], y: Option[A]): Option[A] =
      x match
        case None => y
        case Some(valueX) =>
          y match
            case None => x
            case Some(valueY) => Some(valueX |+| valueY)

  given [A]: Monoid[A => A] with
    def combine(f: A => A, g: A => A): A => A = f compose g
    def empty: A => A = a => a

  object Extensions:
    extension [A](xs: List[A])
      def collectWithM[B](f: A => B)(using B: Monoid[B]): B =
        xs.view.map(f).foldLeft(B.empty)(B.combine)

      def collectM(using Monoid[A]): A =
        collectWithM(x => x)