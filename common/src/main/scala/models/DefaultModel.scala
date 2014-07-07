package models

import world2d.{LivingHexagon, Point, HexaGrid}

object DefaultModel {
  private val t0 = System.currentTimeMillis
  def now: Float = System.currentTimeMillis - t0
  
  // means the world is about 100 * 100 hexas
  val xMax = 50
  val yMax = 50
}

import DefaultModel._

abstract class DefaultModel extends GridModel[LivingHexagon] {
  
  def worldLimits = (new LivingHexagon(-xMax, -yMax).center, new LivingHexagon(xMax, yMax).center)
  
  def worldSize = {
    val limits = worldLimits
    (limits._2.x - limits._1.x, limits._2.y - limits._1.y)
  }

  // methods modifying the state of the model

  def at(p: Point): LivingHexagon = {
    // living hexa are rotated, so switch x and y to match
    HexaGrid.at(Point(p.y, p.x) / LivingHexagon.scaling)
  }

  def atGridCoords(x: Int, y: Int) = new LivingHexagon(x, y)

  private val lockClosure = {
    var lockStatus = true
    val lock = () => lockStatus = true
    val unlock = () => lockStatus = false
    val locked = () => lockStatus
    (lock, unlock, locked)
  }

  def lock() = lockClosure._1()
  def unlock() = lockClosure._2()
  def locked = lockClosure._3()
}
