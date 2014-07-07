package game

import rendering.screen.BenchmarkForegroundScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import hexatext.mvc.controller.BenchmarkController2
import hexatext.mvc.model.FlakeModel

class BenchmarkGame2 extends Game {
  override def create() {
    val model = new FlakeModel(0l)
    val screen = new BenchmarkForegroundScreen(model)
    setScreen(screen)
    Gdx.input.setInputProcessor(new BenchmarkController2(model, screen))
  }
}