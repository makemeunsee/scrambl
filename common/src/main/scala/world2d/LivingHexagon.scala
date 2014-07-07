package world2d

import LivingHexagon._
import scala.language.implicitConversions

object LivingHexagon {
  val xSpacing = Hexagon.ySpacing
  val ySpacing = Hexagon.xSpacing 
  val scaling = 16f
  val hexa0Points = Hexagon(0,0).points map { case Point(x, y) => Point(y, x) * scaling }
  
  val agonyDuration = 666f // ms
  
  import scala.language.implicitConversions
  implicit def stdToLiving(h: Hexagon): LivingHexagon = new LivingHexagon(h, 0, 0)
}

// rotated hexa, with "birth" date for animation effects
class LivingHexagon(x: Int, y: Int, val birthtime: Float = 0, val deathtime: Float = 0) extends Hexagon(x, y) { hex =>
  // same hash and equals as Hexagon to stay comparable
  
  def this(hexa: Hexagon, birth: Float, death: Float) = this(hexa.x, hexa.y, birth, death)
  
  override val center = Point(super.center.y, super.center.x) * scaling
  
  override val points = hexa0Points map ( _ + center )

  override def sextant(p: Point) = {
    new Hexagon(x,y).sextant(Point(p.y, p.x) / scaling)
  }
  
  override def neighbours = super.neighbours map (new LivingHexagon(_, 0, 0))
  
  override def toString = s"${super.toString} - birth: $birthtime - death: $deathtime"
  
  def alive(when: Float) = when > birthtime && when < deathtime
}

