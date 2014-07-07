package ee.uns.makeme

import com.badlogic.gdx.backends.android.AndroidApplication
import android.os.Bundle
import models.roaming.RoamingModel
import game.ScramblGame
import ee.uns.makeme.cfg.AndroidConfig
import rendering.screen.RoamingScreen
import hexatext.mvc.controller.StdController
import renderer.AndroidScramblScreen
import controls.AndroidInputProcessor

class Main extends AndroidApplication {
  
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    initialize(new ScramblGame[RoamingScreen] {
      private val model = new RoamingModel

      def createController(screen: RoamingScreen): StdController = {
        new AndroidInputProcessor(model, screen)
      }

      def createScreen = new AndroidScramblScreen(model)
   }, new AndroidConfig)
  }
}
