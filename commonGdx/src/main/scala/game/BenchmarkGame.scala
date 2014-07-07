package game

import rendering.screen.BenchmarkBackgroundScreen
import hexatext.mvc.model.FlakeModel
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import hexatext.mvc.controller.BenchmarkController

class BenchmarkGame extends Game {
  override def create() {
    val model = new FlakeModel(0l)
    val screen = new BenchmarkBackgroundScreen(model)
    setScreen(screen)
    Gdx.input.setInputProcessor(new BenchmarkController(model, screen))
  }
}