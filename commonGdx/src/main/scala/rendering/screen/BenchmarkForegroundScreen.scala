package rendering.screen

import com.badlogic.gdx.Gdx
import scala.concurrent.duration._
import hexatext.mvc.model.FlakeModel
import models.DefaultModel
import world2d.LivingHexagon
import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh
import rendering.shaders.GradientBasaltShader
import com.badlogic.gdx.graphics.GL20

class BenchmarkForegroundScreen(val model: FlakeModel)
  extends DefaultScreen[LivingHexagon]
  with ForegroundScreen {
  
  var newRender = true
  
  def currentTool = if (newRender) super.blockGenericShader else blockShaderTool
  
  private val blockShaderTool = GradientBasaltShader
  private val blockShader = blockShaderTool.createShaderProgram
  
  private lazy val meshes: Map[Hexagon, Mesh] = model.onGrid
    .map { h => (h, blockShaderTool.createMesh(h)) }
    .toMap
  
  private def renderHexa(hexa: LivingHexagon, now: Float, shrinking: Boolean) {
    val lifetime: Float = now - hexa.birthtime
    val a = if ( lifetime > LivingHexagon.agonyDuration ) 1f else lifetime/LivingHexagon.agonyDuration
    val marginCoeff = if ( shrinking ) 1f-a else a
    if ( marginCoeff > 0 ) {
      blockShader.setUniformf("u_alpha", marginCoeff)
      meshes.get(hexa) match {
        case Some(mesh) =>
          mesh.render(blockShader, GL20.GL_TRIANGLE_FAN)

        case None       => throw new Error
      }
    }
  }
  
  override def render(delta: Float) {
    Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val time = DefaultModel.now

    if (newRender) renderBlocks(0, time)
    else {
      blockShader.begin()
      blockShader.setUniformMatrix("u_worldView", camera.combined)
      model.onGrid map ( renderHexa(_, time, shrinking = false) )
      blockShader.end()
    }
    
    val fps = Gdx.graphics.getFramesPerSecond
    if (fps != 0) perf.storePerf(s"[$currentTool] mean frame duration", (1000000l / fps).micros)
    ()
  }
}