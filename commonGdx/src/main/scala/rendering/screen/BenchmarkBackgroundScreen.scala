package rendering.screen

import com.badlogic.gdx.Gdx
import rendering.shaders.{CubicLavaShader, GdxShaderModule}
import scala.concurrent.duration._
import hexatext.mvc.model.FlakeModel
import models.DefaultModel
import world2d.LivingHexagon

class BenchmarkBackgroundScreen(val model: FlakeModel)
  extends DefaultScreen[LivingHexagon]
  with BackgroundScreen[GdxShaderModule[LivingHexagon], LivingHexagon] {
  
  private var backgroundShader: GdxShaderModule[LivingHexagon] = CubicLavaShader //default value
  
  override def setBackgroundShader(newShaderTool: GdxShaderModule[LivingHexagon], recreateMesh: Boolean = true) {
    backgroundShader = newShaderTool
    super.setBackgroundShader(newShaderTool, recreateMesh)
  }
  
  def updateBlocks(blocks: Set[_ <: LivingHexagon]) {} // no updates
  
  override def render(delta: Float) {
    renderBackground(delta, DefaultModel.now)
    
    val fps = Gdx.graphics.getFramesPerSecond
    if (fps != 0) perf.storePerf(s"[$backgroundShader] mean frame duration", (1000000l / fps).micros)
    ()
  }
}