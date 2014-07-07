package hexatext.mvc.controller

import rendering.screen.BenchmarkForegroundScreen
import com.badlogic.gdx.InputAdapter
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import com.badlogic.gdx.Gdx
import hexatext.mvc.model.FlakeModel
import world2d.LivingHexagon
import controllers.GameController
import rendering.shaders.GdxShadersPack

class BenchmarkController2(val model: FlakeModel, val screen: BenchmarkForegroundScreen) extends InputAdapter with GameController {

  screen.setZoom(screen.zoomMax)
  screen.setBlockShader(GdxShadersPack.LavaBasaltGradient.blockShader)
  model.next()
  onChange(model.toggle(model.flake.transpose.flatten.toSet))
  createFuture()

  def clickAction(hexa: LivingHexagon) {} // do nothing

  private def createFuture() {
    // switch rendering mode every 10 secs
    val f = Future { Thread.sleep(10000l) }
    f onComplete {
      case Success(_) =>
        perf.printResults()
        Gdx.app.postRunnable(new Runnable {
          override def run() {
            screen.newRender = !screen.newRender
            createFuture()
          }
        })
      case Failure(e) =>
        println(s"an error occurred $e")
    }
  }
}