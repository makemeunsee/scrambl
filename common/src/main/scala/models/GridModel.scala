package models

import world2d.{Hexagon, Point, LivingHexagon}

trait GridModel[H <: Hexagon] {

  def at(p: Point): H

  def atGridCoords(x: Int, y: Int): H

  // returns a seq of H, filling the grid from p0 to p1.
  // also returns a function to find the id of an H inside the sequence.
  def window(p0: Point, p1: Point): (Seq[H], (H => Int)) = /*perf.perfed(s"window: $p0, $p1")*/ {
    val h0 = at(p0)
    val h1 = at(p1)
    val hexs = (h0.x to h1.x)
    .flatMap ( x => (h0.y-1 to h1.y+1)
    .map ( y => atGridCoords(x, y) ) )
    
    val orig = hexs(0)
    val height = (h1.y - h0.y) + 3
    val idOf: H => Int = { h => (h.x - orig.x) * height + h.y - orig.y }
    
    (hexs, idOf)
  }
  
  // all hexas in the model
  def onGrid: Set[LivingHexagon]
  
  // corners of the world
  def worldLimits: (Point, Point)
  
  // width and height of the world
  def worldSize: (Float, Float)
  
}