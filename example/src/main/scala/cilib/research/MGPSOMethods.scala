package cilib
package research

import research._
import scalaz._
import Scalaz._

object MGPSOMethods {

  def multiEval(envX: EnvironmentX)(particle: MGParticle) = MGStep.stepPure[Double, MGParticle] {
    val fitness = if (particle.pos.isInbounds) {
      envX.f(particle.pos.pos)
    } else {
      particle.pos.fitness.map(_ => Double.MaxValue)
    }
    particle.updateFitness(fitness)
  }

  def gbest(envX: EnvironmentX)(particle: MGParticle, collection: NonEmptyList[MGParticle]) =
    MGStep.stepPure[Double, PositionX] {
      val x = collection.toList
        .filter(x => x.swarmID == particle.swarmID)
        .sortWith((x, y) => envX.compareAtIndex(x.pb.fitness, y.pb.fitness, x.swarmID))
        .head
        .pb
      //println(x.fitness)
      x
    }

  def pbest(particle: MGParticle) = MGStep.stepPure[Double, PositionX] {
    particle.pb
  }

  def updatePBest(envX: EnvironmentX)(particle: MGParticle) = MGStep.stepPure[Double, MGParticle] {
    if (envX.compare(particle.pos.fitness, particle.pb.fitness)) {
      particle.updatePB
    } else {
      particle
    }
  }

  def updatePBestBounds(envX: EnvironmentX)(p: MGParticle): StepS[Double, MGArchive, MGParticle] =
    if (p.pos.isInbounds) updatePBest(envX)(p) else MGStep.stepPure[Double, MGParticle](p)

  def calcVelocity(particle: MGParticle,
                   social: PositionX,
                   cognitive: PositionX,
                   w: Double,
                   c1: Double,
                   c2: Double,
                   c3: Double) =
    MGStep.withArchive[Double, PositionX](archive => {
      if (archive.isEmpty) {
        Step.liftR(
          for {
            cog <- (cognitive - particle.pos).traverse(x => Dist.stdUniform.map(_ * x))
            soc <- (social - particle.pos).traverse(x => Dist.stdUniform.map(_ * x))
          } yield (w *: particle.velocity) + (c1 *: cog) + (c2 *: soc)
        )
      } else {
        Step.liftR(
          RVar
            .shuffle(archive.values.toNel.get)
            .flatMap(archiveList => {
              val tournament = archiveList.toList.take(3)
              val archiveGuide = CrowdingDistance.leastCrowded(tournament)
              for {
                cog <- (cognitive - particle.pos).traverse(x => Dist.stdUniform.map(_ * x))
                soc <- (social - particle.pos).traverse(x => Dist.stdUniform.map(_ * x))
                arc <- (archiveGuide.pos - particle.pos).traverse(x => Dist.stdUniform.map(_ * x))
                lambda <- particle.lambda.value
              } yield {
                (w *: particle.velocity) + (c1 *: cog) + (lambda *>: (c2 *: soc)) + (lambda.map(x =>
                  1 - x) *>: (c3 *: arc))
              }
            }))
      }
    })

  def updateVelocity(particle: MGParticle, v: PositionX) = MGStep.stepPure[Double, MGParticle] {
    particle.updateVelocity(v)
  }

  def updateLambda(particle: MGParticle) = MGStep.stepPure[Double, MGParticle] {
    particle.updateLambda
  }

  def stdPosition(particle: MGParticle, v: PositionX) = MGStep.stepPure[Double, MGParticle] {
    particle.updatePos(particle.pos + v)
  }

  def insertIntoArchive(particle: MGParticle) =
    MGStep.modifyArchive { archive =>
      archive.insert(particle)
    }

  def mgpso(envX: EnvironmentX)
    : NonEmptyList[MGParticle] => MGParticle => StepS[Double, MGArchive, MGParticle] =
    collection =>
      x =>
        for {
          _ <- insertIntoArchive(x)
          cog <- pbest(x)
          soc <- gbest(envX)(x, collection)
          v <- calcVelocity(x, soc, cog, envX.cp.w, envX.cp.c1, envX.cp.c2, envX.cp.c3)
          p <- stdPosition(x, v)
          p2 <- multiEval(envX)(p)
          p3 <- updateVelocity(p2, v)
          p4 <- updateLambda(p3)
          updated <- updatePBestBounds(envX)(p4)
        } yield updated

}
