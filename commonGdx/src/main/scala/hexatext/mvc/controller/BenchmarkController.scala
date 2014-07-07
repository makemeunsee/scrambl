package hexatext.mvc.controller

import com.badlogic.gdx.InputAdapter
import rendering.screen.BenchmarkBackgroundScreen
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import com.badlogic.gdx.Gdx
import rendering.shaders.{CubicLavaShader, ShadersPack, AutoNoiseShader}
import hexatext.mvc.model.FlakeModel
import world2d.LivingHexagon
import rendering.shaders.{GdxShaderModule, GenericShaderModule, GdxShadersPack}
import GenericShaderModule._

class BenchmarkController(model: FlakeModel, screen: BenchmarkBackgroundScreen) extends InputAdapter {

  private val shaderTools: Seq[GdxShaderModule[LivingHexagon]] =
    Seq(AutoNoiseShader(GdxShadersPack.LavaBasaltCubic.backgroundShader.color0, GdxShadersPack.LavaBasaltCubic.backgroundShader.color1, blendingRate = 10f, cubic = true),
        GdxShadersPack.LavaBasaltCubic.backgroundShader.copy(blendingRate = 10f),
        CubicLavaShader)
  println(shaderTools.zipWithIndex)

  screen.setZoom(screen.zoomMax)
  screen.setBackgroundShader(shaderTools(0))
  createFuture(1)
  
  private def createFuture(shaderIndex: Int): Future[Unit] = {
    // switch rendering mode every 10 secs
    val f = Future { Thread.sleep(10000l) }
    f onComplete {
      case Success(_) =>
        perf.printResults()
        Gdx.app.postRunnable(new Runnable {
          override def run() {
            screen.setBackgroundShader(shaderTools(shaderIndex % shaderTools.size))
            createFuture(shaderIndex+1)
            ()
          }
        })
      case Failure(e) =>
        println(s"an error occurred $e")
    }
    f
  }
}