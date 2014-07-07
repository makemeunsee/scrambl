package controllers

import models.GridModel
import views.View
import world2d.LivingHexagon

// simple controller binding a world(model) and a screen(view)
trait GameController {

  def model: GridModel[LivingHexagon]

  def screen: View[LivingHexagon]

  def clickAction(hexa: LivingHexagon)

  def at(x: Int, y: Int): LivingHexagon = {
    model.at(screen.viewToWorldCoordinates(x, y))
  }

  protected def onChange(changed: Set[LivingHexagon]) {
    screen.updateBlocks(changed)
  }
}