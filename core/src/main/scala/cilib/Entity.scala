package cilib

import scala.language.higherKinds
import scalaz._
import Scalaz._

// Transformer of some sort, over the type F
sealed abstract class Position[F[_], A] {
  def pos =
    this match {
      case Solution(x)  => x
      case Entity(x, _) => x
    }

  def fit =
    this match {
      case Solution(_)  => None
      case Entity(_, f) => Some(f)
    }

  def map[B](f: A => B)(implicit F: Monad[F]): Position[F, B] =
    Solution(pos map f)

  def flatMap[B](f: A => Position[F, B])(implicit F: Monad[F]): Position[F, B] =
    Solution(F.bind(pos)(f(_).pos))

  def zip[B](other: Position[F, B])(implicit M: Monad[F]): Position[F, (A, B)] =
    Solution(for {
      a <- pos
      b <- other.pos
    } yield (a, b))
}

final case class Solution[F[_], A](x: F[A]) extends Position[F, A]
final case class Entity[F[_], A](x: F[A], f: Fit) extends Position[F, A]

object Position {
  import spire.algebra._
  import spire.math._
  import spire.implicits._

  implicit class ToPositionVectorOps[F[_], A: Numeric](x: Position[F, A]) {
    def + (other: Position[F, A])(implicit Z: Zip[F], F: Functor[F]) = Solution {
      Z.zipWith(x.pos, other.pos)(_ + _)
    }
    /*def - (other: Position[F, A])(implicit F: Zip[F]) = Solution(x.pos.zipWith(other.pos)((a, ob) => ob.map(_ - a).getOrElse(a))._2)
    def * (other: Position[F, A])(implicit F: Zip[F]) = Solution(x.pos.zipWith(other.pos)((a, ob) => ob.map(_ * a).getOrElse(a))._2) */
    def *:(scalar: A)(implicit F: Functor[F]) = Solution(x.pos.map(_ * scalar))
  }

  implicit def positionFitness[F[_], A] = new Fitness[Position[F, A]] {
    def fitness(a: Position[F, A]) =
      a.fit
  }

}

object Entity {

  def fromBounds(bounds: List[Interval]): RVar[Solution[Vector, Double]] =
    bounds.traverse(b => Dist.uniform(b.lower, b.upper)).map(_.toVector).map(Solution(_))

  def mkCollection(n: Int, bounds: List[Interval]) =
    fromBounds(bounds) replicateM n

}

final class Interval(val lower: Double, val upper: Double) {
  def ^ (n: Int): List[Interval] =
    (1 to n).map(_ => this).toList
}

object Interval {
  def apply(l: Double, r: Double) =
    new Interval(l, r)
}