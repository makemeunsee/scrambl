// common/src/main/Smile.scala
package game

import com.badlogic.gdx.Game
import rendering.screen.ScramblScreen
import hexatext.mvc.controller.StdController
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Gdx
import hexatext.mvc.controller.ScreenController

abstract class ScramblGame[S <: ScramblScreen] extends Game {
  
  def createScreen: S
  def createController(screen: S): StdController

  // must be first called when context is created, ie in game.create
  lazy val screen = createScreen
  lazy val controller = createController(screen)
  def menu = controller.menu

  override def create() {

    setScreen(screen)
    controller.init()

    val multiplexer = new InputMultiplexer(menu.showMenuAction, menu, controller.inputProcessor)
    Gdx.input.setInputProcessor(multiplexer)
  }
  
  override def resize(width: Int, height: Int) {
    super.resize(width, height)
    menu.resize(width, height)
  }

  override def render() {
    super.render()
    menu.showMenuAction.draw()
    menu.draw()
    controller.continuousActions()
  }
}

abstract class ScramblExplorer[S <: ScramblScreen] extends ScramblGame[S] {
  def createController(screen: S): StdController with ScreenController
}