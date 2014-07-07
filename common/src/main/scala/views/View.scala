package views

import world2d.Hexagon
import world2d.Point
import models.GridModel

// simplest view of an hexagon model
trait View [H <: Hexagon] {
  // underlying model
  protected def model: GridModel[H]
  // convert coordinates on screen to coordinates in world
  def viewToWorldCoordinates(x: Int, y: Int): Point
  // update the view of the given blocks.
  def updateBlocks(blocks: Set[_ <: H]): Unit
}