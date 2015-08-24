package cilib
package example

import cilib.Defaults._

import scalaz.NonEmptyList
import scalaz.effect._
import scalaz.effect.IO.putStrLn
import scalaz.std.list._
import spire.implicits._

object LBestPSO extends SafeApp {

  val sum = Problems.spherical[Double]

  // LBest is a network topology where every Paricle 'x' has (n/2) neighbours
  // on each side. For example, a neighbourhood size of 3 means that there is
  // a single neighbour on both sides of the current particle.

  // Define a LBest PSO and run it for a single iteration
  val cognitive: Guide[Mem[Double],Double] = Guide.pbest
  val social: Guide[Mem[Double],Double] = Guide.lbest[Mem[Double]](3)

  val lbestPSO = gbest(0.729844, 1.496180, 1.496180, cognitive, social)

  val swarm = Position.createCollection(PSO.createParticle(x => Entity(Mem(x, x.zeroed), x)))(Interval(closed(-5.12),closed(5.12))^30, 20)
  val a = Step.pointR[Double,List[Particle[Mem[Double],Double]]](swarm)

  val b2 = Iteration.sync(lbestPSO)
  val w = a flatMap (b2.run)
  val m = w.run(Comparison.quality(Min))(sum)
  val z = m.run(RNG.fromTime)

  // Run the above algorithm 1000 times, without any parameter changes
  //val r = b2.repeat(1000)
  //val w2 = a flatMap (r)
  //val m2 = w2 run Env(Min, sum)
  //val y2 = m2 run sum
  //val z2 = m2.run(RNG.fromTime)

  //println(z2)

  override val runc: IO[Unit] =
    putStrLn(z.toString)
}
