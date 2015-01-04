package cilib
package example

import cilib.Predef._

import scalaz._
import scalaz.syntax.foldable._
import scalaz.std.list._
import scalaz.contrib.spire.conversions.toScalaz._

object GBestPSO {

  def main(args: Array[String]): Unit = {
    val sum = Problem.static((a: List[Double]) => Valid(a.foldMap(x => x*x)))

    // Define a normal GBest PSO and run it for a single iteration
    val cognitive: Guide[Mem[Double],Double] = Guide.pbest
    val social: Guide[Mem[Double],Double] = Guide.gbest

    val gbestPSO = gbest[Mem[Double]](0.729844, 1.496180, 1.496180, cognitive, social)
    val a = Instruction.pointR(Position.createCollection(PSO.createParticle(x => (Mem(x,x.map(_ => 0.0)), x)))(Interval(closed(-5.12),closed(5.12))^30, 20))

    val b2 = Iteration.sync(gbestPSO)
    val w = a flatMap (b2.run)
    val m = w.run(cilib.Env(Min, sum))
//    val y = m run sum
    val z = m.run(RNG.fromTime)

    println(z)

    // Run the above algorithm 1000 times, without any parameter changes
    val r = b2.repeat(1000)
    val w2 = a flatMap (r)
    val m2 = w2 run Env(Min, sum)
    //val y2 = m2 run sum
    val z2 = m2.run(RNG.fromTime)

    println(z2)
  }
}
