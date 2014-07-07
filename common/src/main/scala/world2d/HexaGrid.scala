package world2d

import scala.util.Random
import Hexagon.{Route, Neighbour, NeighbourN, NeighbourS, NeighbourSE, NeighbourSW, NeighbourNE, NeighbourNW}
import scala.annotation.tailrec

object HexaGrid {
  
  val xStep = Hexagon.xSpacing
  val yStep = Hexagon.ySpacing / 2
  
  def at(p: Point): Hexagon = {
    val col = (p.x / xStep).toInt - (if (p.x < 0) 1 else 0)
    val row = (p.y / yStep).toInt - (if (p.y < 0) 1 else 0)
    // depending on row and column modulo, the hexagons to choose from differ
    val (h1, h2) = (col % 2, row % 2) match {
      case (0, 0) => (Hexagon(col, row/2),     Hexagon(col+1, row/2+1))
      case (0, _) => (Hexagon(col, (row+1)/2), Hexagon(col+1, (row+1)/2))
      case (_, 0) => (Hexagon(col, row/2+1),   Hexagon(col+1, row/2))
      case (_, _) => (Hexagon(col, (row+1)/2), Hexagon(col+1, (row+1)/2))
    }
    // check distance to center of possible hexagons
    val v1 = h1.center - p
    val v2 = h2.center - p
    if ( v1.x*v1.x + v1.y*v1.y < v2.x*v2.x + v2.y*v2.y ) h1
    else h2
  }

  def window[T: Numeric](n1: T, n2: T, n3: T, n4: T): Seq[Hexagon] = {
    window(Point(n1, n2), Point(n3, n4))
  }
  def window(p1: Point, p2: Point) = {
    val h1 = at(p1)
    val h2 = at(p2)
    (math.min(h1.x, h2.x)-1 to math.max(h1.x, h2.x)+1)
    .flatMap(x =>
      (math.min(h1.y, h2.y)-1 to math.max(h1.y, h2.y)+1)
      .map(y => Hexagon(x,y) ) )
  }

  // indicate if a hexagon (toLocate) is inside the arc of neighbours of center, starting at initialDir(center), stopping at stopAt
  // 'stopAt' and 'toLocate' must be neighbours of 'center'.
  // indicates if 'toLocate' is found among the neighbours of 'center',
  // looking from 'center(initialDir)' up to 'stopAt' (excluded).
  // 'direction' indicates clockwise (<0) or counterclockwise (>=0) search
  @tailrec
  def locate(center: Hexagon, initialDir: Neighbour, stopAt: Hexagon, direction: Int, toLocate: Hexagon): Boolean = {
    if ( initialDir(center) == stopAt ) false
    else if ( initialDir(center) == toLocate ) true
    else if ( direction < 0 ) locate(center, initialDir.previous, stopAt, direction, toLocate)
    else locate(center, initialDir.next, stopAt, direction, toLocate)
  }
  
  // from a seq of hexas, return its symmetrical seq using symmetry center (0,0)
  def symmetry(hexas: Seq[Hexagon]) = hexas.map (_.symmetrical)
  
  // given the path between 3 consecutive hexas (ie 2 directions / neighbour objects),
  // return the angle made by these 3 hexas
  // returns the angle as sixths of the circle (1 unit = 60°), positive if < 180°, negative if > 180°
  // u turn is not accepted
  @tailrec
  def angle(n1: Neighbour, n2: Neighbour): Int = (n1, n2) match {
    case (NeighbourN, NeighbourN)  => 0
    case (NeighbourN, NeighbourNW) => 1
    case (NeighbourN, NeighbourSW) => 2
    case (NeighbourN, NeighbourS)  => throw new Error("no turning back")
    case (NeighbourN, NeighbourSE) => -2
    case (NeighbourN, NeighbourNE) => -1
    case (x, y)                    => angle(x.next, y.next)
  }
  
  // given a proper loop (a sequence of at least 3 consecutive hexagons, with its first and its last being neighbours),
  // return the overall angle made by the loop (6 or -6, see angle function details)
  def loopAnglesSum(loopingSeq: Seq[Hexagon]) = {
    @tailrec
    def loopRec(loop: Seq[Hexagon], acc: Int): Int = {
      loop match {
        case h1 +: h2 +: h3 +: tail => loopRec( h2 +: h3 +: tail, acc + angle((h1 toNeighbour h2).get, (h2 toNeighbour h3).get))
        case _ => acc
      }
    }
    loopRec(loopingSeq :+ loopingSeq.head :+ loopingSeq.tail.head, 0)
  }
  
  // from the given hexagon, expand until blocked by the walls
  // ! warning ! termination guaranteed only if walls are made of contiguous hexagons,
  // with no hole, and from is within the walls
  def allWithin(from: Hexagon, walls: Set[Hexagon]) = {
    @tailrec
    def grow(front: Set[Hexagon], acc: Set[Hexagon]): Set[Hexagon] = {
      if (front.isEmpty) acc
      else grow(front.flatMap(_.neighbours) -- front -- walls -- acc, acc ++ front)
    }
    grow(Set(from), Set.empty)
  }
  
  // a distribution of neighbour used when generating a flake
  type NeighbourDistribution = Map[Neighbour, Int]

  // standard, fair distribution of neighbour
  val unbiasedDistribution: NeighbourDistribution = Hexagon.neighbours.map((_, 1)).toMap

  // a flake pattern is a list of steps, used to grow a branch of a flake from a center hexagon, one after another.
  // symmetries or rotations of the steps can be used to grow a 6 branches flake.
  type FlakePattern = Seq[Hexagon.Neighbour]

  // a flake is a sequence of 6 branches, symmetrical with center H(0,0), non overlapping.
  // a branch is a list of consecutive hexagons.
  type Flake[H <: Hexagon] = Seq[Seq[H]]

  // a function type used to provide a limit for the flake generation algorithm.
  // expected parameters: a hexagon and its position in a flake branch
  // expected returned value: true while flake generation must go on
  type FlakeGenerationStopper = (Int, Hexagon) => Boolean

  // the center of the world
  private val h0 = Hexagon(0,0)

  // create a random flake pattern.
  // the stopper is used to stop the pattern generation.
  // hexagons input to the stopper are of the 'reference' branch of the flake, as if built from center h0.
  // the random instance has to be provided, so that this function remains deterministic.
  // a neighbour distribution can be specified to shape the flake.
  // warning: distributions missing completely some neighbours can lead to impossible flakes, ie thrown errors
  def rndFlake(stopper: FlakeGenerationStopper)
              (implicit rnd: Random,
               distribution: NeighbourDistribution): FlakePattern = /*perf.perfed("rndFlake")*/ {
    // at the beginning, a flake only has its center, and can grow in any direction
    flakeGeneration(stopper = stopper).reverse
  }

  // recursive flake generation.
  // branch0Pattern: the accumulator of this function, the eventually returned flake pattern.
  // branch0AndSiblings: 3 branches actually built during the flake generation, used to ensure correctness.
  // acceptablesDirs: directions/neighbours acceptable for the next step in the flake pattern.
  // stopper: when to stop.
  @tailrec
  private def flakeGeneration(branch0Pattern: FlakePattern = Seq.empty[Neighbour],
                              branch0AndSiblings: (Seq[Hexagon],Seq[Hexagon],Seq[Hexagon]) = (Seq(h0), Seq(h0), Seq(h0)),
                              acceptablesDirs: Seq[Neighbour] = Hexagon.neighbours,
                              stopper: FlakeGenerationStopper)
                             (implicit rnd: Random,
                              distribution: NeighbourDistribution): Seq[Neighbour] = {
    val (left, branch, right) = branch0AndSiblings
    // last hexa in the branch
    val lastStep = branch.last

    val initialDir = branch0Pattern.headOption

    // to prevent dead lock
    if (acceptablesDirs.isEmpty) throw new Error("impossible maze")

    // create a distribution of neighbours
    val redistributed = Hexagon.neighbours
      .filter(acceptablesDirs.contains)                 // keep only acceptables
      .map( n => Seq.fill( distribution(n) )( n ) )     // apply distribution
      .flatten

    // pick a random next step among the redistributed, acceptable neighbours
    val nextDir = redistributed(rnd.nextInt(redistributed.size))
    val nextStep = nextDir(branch.head)

    // apply the new step to the branches
    val newLeft = nextDir.next(left.head) +: left
    val newBranch = nextStep +: branch
    val newRight = nextDir.previous(right.head) +: right

    val branchSize = branch.size
    if (stopper(branchSize, nextStep)) {
      // compute new acceptables
      flakeGeneration(nextDir +: branch0Pattern,
        (newLeft, newBranch, newRight),
        acceptableNextSteps(branch, Seq(newLeft, newRight), nextDir),
        stopper)
    } else {
      println(s"flake generation stopped at length $branchSize")
      branch0Pattern
    }
  }

  // given a flake branch (sequence of contiguous hexagons), its left and right neighbour branches
  // and a new step, contiguous to the last hexagon of the branch,
  // return which neighbours of the new step lead to dead ends
  private def acceptableNextSteps(path: Seq[Hexagon],
                                  siblings: Seq[Seq[Hexagon]],
                                  nextDir: Hexagon.Neighbour): Seq[Hexagon.Neighbour] = {

    // a dead end is a loop created by the path either with itself or one of its siblings


    // look at all neighbours but the last step
    val lastStep = path.head
    val newStep = nextDir(lastStep)
    val newNeighbours = newStep.neighbours
      .filter { _ != lastStep }

    // find which of the neighbours are part of a loop of the branch with itself
    val loopSigs1 = newNeighbours
      .map { n => (n, path.indexOf(n)) }     // find the neighbours position on the path
      .filter( _._2 > -1 )                   // if they're actually on the path
      .map { case (n, id) =>                 // it indicates a loop
      val loop = newStep +: path.take(id+1)  // the loop goes from the neighbour to the new step
      (n, loopAnglesSum(loop))               // determine the loop's orientation
    }
    // find which of the neighbours are part of a loop of the branch with a sibling
    val loopSigs2 = newNeighbours
      .filter(n => loopSigs1.forall(_._1 != n)) // avoid looking at the neighbours part of the branch
      .map { n =>                               // find loops with the path and the siblings
      (n, siblings
          .map(s => (s, s.indexOf(n)))          // find the neighbours position on the sibling branches
          .find(_._2 > -1) ) }
      .filter { _._2 != None }                  // if they're actually on one of the siblings
      .map { case (n, Some((sibling, id))) =>   // it indicates a loop with the path and a sibling
      val loop = (newStep +: path) ++           // the loop goes from the origin to the new step
        sibling.drop(id).reverse.tail           // through the neighbour, back to the origin (h0 excluded to avoid duplicate)
      (n, loopAnglesSum(loop))                  // determine the loop's orientation
    }

    val n0 = nextDir.next.next.next             // reverse the step direction, as we reason from the newStep point of view henceforth

    val loopSigs = loopSigs1 ++ loopSigs2       // concatenate once all loop signatures

    newNeighbours             // look at all the potential next steps
    .filter { h =>
      loopSigs.forall { case (n, dir) =>
        n != h &&                               // remove those making the loops (they're part of the branch or its siblings)
        !locate(newStep, n0, n, dir, h)         // remove those inside of a loop
      }
    }
    .map(n => (newStep toNeighbour n).get)      // translate neighbours (hex) to neighbours (directions)
  }

  private def djikstra(rem: Set[Hexagon],
                       currentFront: Map[Hexagon, Route],
                       currentDistance: Int,
                       end: Hexagon): Option[Route] = {
    if (currentFront.isEmpty) None
    else if (currentFront contains end) currentFront.get(end)
    else if (rem.isEmpty) None
    else {
      val newRem = rem -- currentFront.keySet
      val newFront = currentFront
      .map { case (h ,r) => h.neighboursWithDir
                            .map { case (n, nextH) => (nextH, r :+ n)} }
      .flatten
      .toMap
      .filter { case (h, _) => rem contains h }
      djikstra(newRem, newFront, currentDistance+1, end)
    }
  }
  
  def path(set: Set[Hexagon], start: Hexagon, end: Hexagon): Option[Route] = {
    if (!(set contains start) || !(set contains end)) None
    else djikstra(set, Map((start, Hexagon.EmptyRoute)), 0, end)
  }
  
  private def djikstraClosest(rem: Set[Hexagon],
                              currentFront: Map[Hexagon, Route],
                              currentDistance: Int,
                              closest: (Hexagon, Route), end: Hexagon): (Hexagon, Route) = {
    val newClosest = currentFront.foldLeft(closest){ case ((oldH, oldR), (h, r)) =>
      val oldVec = oldH.center - end.center
      val vec = h.center - end.center
      if ((oldVec dot oldVec) > (vec dot vec)) (h, r)
      else (oldH, oldR)
    }
    if (currentFront.isEmpty) newClosest
    else if (currentFront contains end) (end, currentFront(end))
    else if (rem.isEmpty) newClosest
    else {
      val newRem = rem -- currentFront.keySet
      val newFront = currentFront
      .map { case (h ,r) => h.neighboursWithDir
                            .map { case (n, nextH) => (nextH, r :+ n)} }
      .flatten.toMap.filter { case (h, _) => rem contains h }
      djikstraClosest(newRem, newFront, currentDistance+1, newClosest, end)
    }
  }

  def pathToClosest(set: Set[Hexagon], start: Hexagon, end: Hexagon): (Hexagon, Route) = {
    val defaultSolution = (start, Hexagon.EmptyRoute)
    if (!(set contains start)) defaultSolution
    else djikstraClosest(set, Map(defaultSolution), 0, defaultSolution, end)
  }
}