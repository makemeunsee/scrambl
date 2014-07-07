package ee.uns.makeme.launchers

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import game.ScramblGame
import rendering.screen.RoamingScreen
import ee.uns.makeme.cfg.DesktopConfig
import ee.uns.makeme.controller.DesktopRoamingController
import models.roaming.RoamingModel

object Roaming extends App {
  new LwjglApplication(new ScramblGame[RoamingScreen] {

    private val model = new RoamingModel

    def createScreen = new RoamingScreen(model)

    def createController(screen: RoamingScreen) = {
      new DesktopRoamingController(model, screen)
    }
  }, new DesktopConfig)
}
