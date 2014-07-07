package ee.uns.makeme.launchers

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import ee.uns.makeme.cfg.DesktopConfig
import ee.uns.makeme.controller.DesktopInputController
import game.ScramblGame
import hexatext.mvc.model.FlakeModel
import rendering.screen.ScramblScreen
import hexatext.mvc.controller.{ScreenInputProcessor, DefaultScreenInputProcessor, DefaultScramblController}
import rendering.shaders.GdxShadersPack

object Main extends App {
  new LwjglApplication(new ScramblGame[ScramblScreen] {

    private val model = new FlakeModel

    def createScreen = new ScramblScreen(model)

    def createController(screen: ScramblScreen) = {
      new DefaultScramblController(model, screen) { controller =>
        override val inputProcessor: ScreenInputProcessor = new DefaultScreenInputProcessor(screen, controller) with DesktopInputController
        override def init(): Unit = {
          super.init()
          model.resizeFlake(50)
          screen.setBackgroundShader(GdxShadersPack.Sunset2.backgroundShader)
          screen.setBlockShader(GdxShadersPack.Sunset2.blockShader)
          nextLevel()
        }
      }
    }
  }, new DesktopConfig)
}