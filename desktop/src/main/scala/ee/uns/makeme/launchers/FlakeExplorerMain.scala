package ee.uns.makeme.launchers

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import hexatext.mvc.model.FlakeExplorerModel
import rendering.screen.ScramblScreen
import ee.uns.makeme.controller.ExplorerInputController
import ee.uns.makeme.cfg.DesktopConfig
import game.ScramblExplorer

object FlakeExplorerMain extends App {

  new LwjglApplication(new ScramblExplorer[ScramblScreen] {

    //new BufferedReader(new InputStreamReader(System.in)).readLine()

    private val model = new FlakeExplorerModel

    def createScreen = new ScramblScreen(model)
    
    def createController(screen: ScramblScreen) = { new ExplorerInputController(model, screen) }
  }, new DesktopConfig)
}
