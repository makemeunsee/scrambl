package ee.uns.makeme.controller

import hexatext.mvc.model.FlakeExplorerModel
import com.badlogic.gdx.Input.Keys
import hexatext.mvc.controller.{ScreenInputProcessor, DefaultScreenInputProcessor, DefaultScramblController, ScreenController, inputSpamProof}
import com.badlogic.gdx.Gdx
import widgets.ExplorerMenu
import rendering.screen.ScramblScreen
import models.FlakeGenerator
import world2d.LivingHexagon
import rendering.shaders.GenericShaderModule

class ExplorerInputController(override val model: FlakeExplorerModel, override val screen: ScramblScreen)
    extends DefaultScramblController(model, screen)
    with ScreenController {

  override val inputProcessor: ScreenInputProcessor = new DefaultScreenInputProcessor(screen, this) with DesktopInputController {
    override def keyUp(key: Int): Boolean = {
      if ( key == Keys.RIGHT ) {
        nextPredefShader()
        true
      }  else if ( key == Keys.LEFT ) {
        previousPredefShader()
        true
      }  else if ( key == Keys.S ) {
        reseedShaders()
        true
      }  else if ( key == Keys.Q ) {
        randomizeBlockColor()
        true
      } else if ( key == Keys.W ) {
        randomizeBackgroundColors()
        true
      } else if ( key == Keys.R ) {
        randomizeBlockColor()
        randomizeBackgroundColors()
        true
      } else if ( key == Keys.SPACE) {
        if (!model.locked) {
          nextLevel()
          true
        } else false
      } else super.keyUp(key)
    }
  }

  import widgets.skin
  override val menu = new ExplorerMenu(this)

  override def init() {
    super.init()
    model.resizeFlake(FlakeGenerator.sizeMax * 3 / 4)
    menu.flakeSizeSlider.setValueSilently(model.flakeSize)
    applyPredefShader()
    nextLevel()
  }

  override def afterNextLevel() {
    // do nuthing
  }

  override def continuousActions() = inputSpamProof("flake resize") {
    if (Gdx.input.isKeyPressed(Keys.DOWN)) {
      flakeSize(model.flakeSize - 1)
    } else if (Gdx.input.isKeyPressed(Keys.UP)) {
      flakeSize(model.flakeSize + 1)
    }
  }

  override protected def applyBlockShader(shader: GenericShaderModule, recreateMesh: Boolean = true) {
    super.applyBlockShader(shader, recreateMesh)
    menu.colorWidget1.updateColor(shader.color0.baseColor)
  }

  override protected def applyBackgroundShader(shader: GenericShaderModule, recreateMesh: Boolean = true) {
    super.applyBackgroundShader(shader, recreateMesh)
    menu.colorWidget2.updateColor(shader.color0.baseColor)
    menu.colorWidget3.updateColor(shader.color1.baseColor)
  }

  override def flakeSize(i: Int) {
    super.flakeSize(i)
    menu.flakeSizeSlider.setValueSilently(model.flakeSize)
  }
  
  override def clickAction(hexa: LivingHexagon) {
    if (!model.locked) {
      screen.updateBlocks(model.toggle(Set(hexa)))
//      toggleHexa(hexa)
    }
  }
  
//  private def toggleHexa(hexa:Hexagon) {
//    val udpatedBlocks = model.toggle(Set(hexa) ++ hexa.symmetries(Hexagon(0,0)))
//    screen.updateBlocks(udpatedBlocks)
//  }
}
