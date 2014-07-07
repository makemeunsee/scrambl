package rendering.screen

import rendering.shaders.{GdxShadersPack, GenericShaderModule}
import world2d.LivingHexagon
import models.DefaultModel

class ScramblScreen(val model: DefaultModel)
  extends DefaultScreen[LivingHexagon]
  with BackgroundScreen[GenericShaderModule, LivingHexagon]
  with ForegroundScreen {
  
  // provide background shader to controller
  private var backgroundShader: GenericShaderModule = GdxShadersPack.LavaBasaltGradient.backgroundShader //default value
  
  def backgroundGenericShader = backgroundShader
  
  override def setBackgroundShader(newShaderTool: GenericShaderModule, recreateMesh: Boolean = true) {
    backgroundShader = newShaderTool
    super.setBackgroundShader(newShaderTool, recreateMesh)
  }

  override def render(delta: Float) {
    val time = DefaultModel.now

    renderBackground(delta, time)
    renderBlocks(delta, time)
    renderHud(delta, time)

    // debug code: show world box
//    val rdr = new ShapeRenderer
//    rdr.setProjectionMatrix(camera.combined)
//    rdr.setColor(0,1,0,1)
//    rdr.begin(ShapeType.Line)
//    rdr.rect(-model.worldSize._1/2+1, -model.worldSize._2/2+1, model.worldSize._1-2, model.worldSize._2-2)
//    rdr.end()

  }
}