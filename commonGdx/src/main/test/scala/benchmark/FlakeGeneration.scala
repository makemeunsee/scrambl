package benchmark

import world2d.{LivingHexagon, Hexagon, HexaGrid}

import scala.util.Random

object FlakeGeneration {

  def main(args: Array[String]) {
    val stopper = (i: Int, h: Hexagon) => i < 500
    implicit val (rnd, dist) = (new Random, HexaGrid.unbiasedDistribution)
    val scale = 2
    for (i <- 1 to 30) {
      perf.perfed("flakefilling") {
        HexaGrid.rndFlake(stopper)                             // generate with class stopper
        .foldLeft( Seq.fill(6)(Seq(new LivingHexagon(0,0))) )  // build each branch from h0
        { case (flake, step) =>
          step.nexts                                           // extend step with rotations (=nexts) to provide all branches with a step
          .zip(flake)                                          // bind the steps to the branches
          .map { case (n, b) =>
            (0 until scale)                             // apply scaling here, by stepping multiple times
            .foldLeft(b) { case (newBranch, _) =>
              new LivingHexagon(n(newBranch.head), 0, 0) +: newBranch  // build branch in reverse (root at the bottom of the list) and reverse it later
            }
          }
        }
        .map(_.reverse.tail) // reverse the branch so the root is head; ignore the root (which is present on all branches)
      }
    }
    perf.printResults()
  }

}
