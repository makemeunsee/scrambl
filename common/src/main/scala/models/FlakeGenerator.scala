package models

import world2d.Hexagon.{NeighbourNE, NeighbourN}

import scala.util.{Random, Try, Success, Failure}
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import world2d.{Hexagon, HexaGrid, LivingHexagon}
import world2d.HexaGrid.{NeighbourDistribution, Flake}

object FlakeGenerator {
  val sizeMax = 500
}

import FlakeGenerator._

// provide flakes on demand.
// prepare the next flake asynchronously after every flake change.
trait FlakeGenerator {
  def seed0: Long
  private var nextSeed  = seed0
  // initial values rewritten at each next flake
  private var v_currentSeed  = 0l
  def currentSeed = v_currentSeed
  private var currentFlake: Flake[LivingHexagon] = Seq.empty[Seq[LivingHexagon]]
  
  def flake: Flake[LivingHexagon] = currentFlake

  // compute first flake right away
  private var nextFlake: Future[Try[Flake[LivingHexagon]]] = nextFlakeFuture
  
  def compact: Boolean
  protected val flakeSpacing = if (compact) 1 else 2

  protected implicit def distri: NeighbourDistribution = HexaGrid.unbiasedDistribution
//  Hexagon.neighbours.map {
//    case NeighbourN  => (NeighbourNE, 0)
//    case NeighbourNE => (NeighbourN, 1)
//    case n           => (n ,0)
//  }.toMap

  protected implicit def stopper = (id: Int, hex: Hexagon) => id < sizeMax / flakeSpacing // grow until size is reached

  private def nextFlakeFuture: Future[Try[Flake[LivingHexagon]]] = {
    // use specific seed
    println(s"next flake seed: $nextSeed")
    Future { Try {
      val h0 = new LivingHexagon(0, 0)
      implicit val rnd = new Random(nextSeed)
      HexaGrid.rndFlake(stopper)                             // generate with class stopper
      .foldLeft( Seq.fill(6)(Seq(new LivingHexagon(0,0))) )  // build each branch from h0
      { case (flake, step) =>
        step.nexts                                           // extend step with rotations (=nexts) to provide all branches with a step
        .zip(flake)                                          // bind the steps to the branches
        .map { case (n, b) =>
          (0 until flakeSpacing)                             // apply scaling here, by stepping multiple times
          .foldLeft(b) { case (newBranch, _) =>
            new LivingHexagon(n(newBranch.head), 0, 0) +: newBranch  // build branch in reverse (root at the bottom of the list) and reverse it later
          }
        }
      }
      .map(_.reverse) // reverse the branch so the root is head
    } }
  }
  
  def next() {
    v_currentSeed = nextSeed
    val rnd = new Random(currentSeed)
    nextSeed = rnd.nextLong()
    // load flake
    currentFlake = Await.result(nextFlake, Duration.Inf) match  {
      case Success(f) => f
      case Failure(t) => throw t
    }
    // start computing next flake
    nextFlake = nextFlakeFuture
  }
}