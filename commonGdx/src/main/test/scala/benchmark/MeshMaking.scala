package benchmark

import models.roaming.RoamingModel
import rendering.shaders._
import world2d.Point

object MeshMaking {

  def main(args: Array[String]) {

    val (maxWidth, maxHeight) = (1920, 1080)
    val viewRatio = maxWidth / maxHeight

    val model = new RoamingModel()
    val (worldWidth, worldHeight) = model.worldSize
    val worldHexas = {
      // extends worlds hexas from model to match view ratio
      // ensures at least the world is displayable on screen
      val worldRatio = worldWidth / worldHeight
      val viewWidth = if (worldRatio < viewRatio) worldWidth * viewRatio / worldRatio else worldWidth
      val viewHeight = if (worldRatio < viewRatio) worldHeight else worldHeight / viewRatio * worldRatio
      model.window(Point(-viewWidth/2, -viewHeight/2), Point(viewWidth/2, viewHeight/2))
    }._1
    for (i <- 1 to 50) GdxShadersPack.LavaBasaltGradient.backgroundShader.verticeAndIndice(worldHexas)
    perf.printResults()
  }

}
