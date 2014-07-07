package world2d

import scala.util.{Failure, Success, Try}

object Hexagon {

  private val piBy3 = math.Pi / 3f
  private val piTimes2By3 = 2 * math.Pi / 3f

  val xSpacing = 1.5f
  private val sqrt3by2 = math.sqrt(3)/2f
  val ySpacing = 2 * sqrt3by2
  
  sealed trait Neighbour {
    def next: Neighbour
    val nexts: Seq[Neighbour] = (0 until 6).map(i => (0 until i).foldLeft(this)((r, _) => r.next))
    def previous: Neighbour
    def apply(h: Hexagon): Hexagon  
  }
  case object NeighbourNE extends Neighbour {
    def next = NeighbourN
    def previous = NeighbourSE
    def apply(h: Hexagon) = Hexagon(h.x+1, h.y+1+h.offset)
  }
  case object NeighbourN  extends Neighbour {
    def next = NeighbourNW
    def previous = NeighbourNE
    def apply(h: Hexagon) = Hexagon(h.x, h.y+1)
  }
  case object NeighbourNW extends Neighbour {
    def next = NeighbourSW
    def previous = NeighbourN
    def apply(h: Hexagon) = Hexagon(h.x-1, h.y+1+h.offset)
  }
  case object NeighbourSW extends Neighbour {
    def next = NeighbourS
    def previous = NeighbourNW
    def apply(h: Hexagon) = Hexagon(h.x-1, h.y+h.offset)
  }
  case object NeighbourS  extends Neighbour {
    def next = NeighbourSE
    def previous = NeighbourSW
    def apply(h: Hexagon) = Hexagon(h.x, h.y-1)
  }
  case object NeighbourSE extends Neighbour {
    def next = NeighbourNE
    def previous = NeighbourS
    def apply(h: Hexagon) = Hexagon(h.x+1, h.y+h.offset)
  }
  val neighbours: Seq[Neighbour] = Seq(NeighbourNE, NeighbourN, NeighbourNW, NeighbourSW, NeighbourS, NeighbourSE)
  
  private object UnitHexagon {
    val east      = Point(1,0)
    val northEast = Point(0.5f, sqrt3by2)
    val northWest = Point(-0.5f, sqrt3by2)
    val west      = Point(-1f,0)
    val southWest = Point(-0.5f,-sqrt3by2)
    val southEast = Point(0.5f,-sqrt3by2)
    val points = Seq(east, northEast, northWest, west, southWest, southEast)
  }
  
  private val pattern = "^([-]{0,1}[0-9]+),([-]{0,1}[0-9]+)$".r
  def apply(str: String): Option[Hexagon] = {
    val matches = pattern.findAllIn(str)
    if (matches.hasNext) Some(Hexagon(matches.group(1).toInt, matches.group(2).toInt))
    else None
  }

  def fromFile(path: String): Seq[Hexagon] = {
    val stream = getClass.getClassLoader.getResourceAsStream(path)
    val source_? = Try(scala.io.Source.fromInputStream(stream))
    source_?.map(_.mkString.split("\\s+")) match {
      case Success(lines) =>
        stream.close()
        lines.foldLeft(Seq.empty[Hexagon]) {
          case (acc, str) =>
            this(str) match {
              case Some(h) => h +: acc
              case _ => acc
            }
        }

      case Failure(e) =>
        println(s"$path could not be read: $e")
        Seq.empty
    }
  }
  
  type Route = Seq[Neighbour]
  
  val EmptyRoute: Route = Seq.empty
  
  def reverseRoute(route: Route) = route.reverse.map {
    case NeighbourN  => NeighbourS
    case NeighbourNE => NeighbourSW
    case NeighbourNW => NeighbourSE
    case NeighbourS  => NeighbourN
    case NeighbourSE => NeighbourNW
    case NeighbourSW => NeighbourNE
  }
  
  def route(from: Hexagon, to: Hexagon, acc: Route = Seq.empty): Route = {
    if (from == to) acc
    else if(from.y < to.y) route(from, NeighbourS(to), acc :+ NeighbourN)
    else if(from.y > to.y) route(from, NeighbourN(to), acc :+ NeighbourS)
    else if(from.x < to.x) route(from, NeighbourNW(to), acc :+ NeighbourSE)
    else route(from, NeighbourNE(to), acc :+ NeighbourSW)
  }
  
  def rotations(route: Route): Seq[Route] = {
    (0 until 6) map { i =>
      route map { n =>
        val origId = neighbours.indexOf(n)
        neighbours((origId + i) % 6)
      }
    }
  }

  def rotate(route: Route, times: Int): Route = {
    route
    .map { n =>
      (0 until math.abs(times))
      .foldLeft(n) { case (result, _) =>
        if (times > 0) result.next
        else result.previous
      }
    }
  }
  
  implicit val ordering: Ordering[Hexagon] = Ordering.by(e => (e.x, e.y))
}

import Hexagon._

case class Hexagon(x: Int, y: Int) {
  
  protected val offset = if (x % 2 == 0) 0 else -1
  
  def center: Point = Point(x * xSpacing, y * ySpacing + offset * ySpacing / 2)
  
  def east: Point      = center + UnitHexagon.east
  def northEast: Point = center + UnitHexagon.northEast
  def northWest: Point = center + UnitHexagon.northWest
  def west: Point      = center + UnitHexagon.west
  def southWest: Point = center + UnitHexagon.southWest
  def southEast: Point = center + UnitHexagon.southEast
  
  def points = Seq(east, northEast, northWest, west, southWest, southEast)
  
  // if h is actually neighbour to this, return the neighbour object from this to h
  def toNeighbour(h: Hexagon): Option[Neighbour] = {
    Hexagon.neighbours.find(_(this) == h)
  }
  
  def routeTo(to: Hexagon) = Hexagon.route(this, to)
  def routeFrom(from: Hexagon) = Hexagon.route(from, this)
  
  def toRoute = Hexagon.route(Hexagon(0,0), this)
  
  def apply(route: Route): Hexagon = route.foldLeft(this) { case (hex, neighbour) => neighbour(hex) }
  
  def neighboursWithDir: Seq[(Neighbour, Hexagon)] =
    Hexagon.neighbours
    .map { n => (n, n(this)) }
  
  def neighbours = neighboursWithDir.map(_._2)
  
  def neighboursMap = neighboursWithDir.toMap

  def symmetrical: Hexagon = Hexagon(-x, -y - offset)

  // extremely bad implementation, very slow...
  def rotation(rotCenter: Hexagon, times: Int): Hexagon = {
    val route = Hexagon.rotate(routeFrom(rotCenter), times)
    rotCenter(route)
  }

  // determine in which sextant the point is, with the world centered on this hexagon
  def sextant(point: Point): Option[Neighbour] = {
    if (point == center) None
    else {
      val diff = point - center
      val angle = math.atan2(diff.y, diff.x)
      if (angle > 0 && angle <= piBy3) Some(NeighbourNE)
      else if (angle > piBy3         && angle <= piTimes2By3) Some(NeighbourN)
      else if (angle > piTimes2By3   && angle <= math.Pi)     Some(NeighbourNW)
      else if (angle >= -math.Pi     && angle < -piTimes2By3) Some(NeighbourSW)
      else if (angle >= -piTimes2By3 && angle < -piBy3)       Some(NeighbourS)
      else if (angle >= -piBy3       && angle < 0)            Some(NeighbourSE)
      else None
    }
  }

  override def toString: String = s"Hex($x,$y)"
}
