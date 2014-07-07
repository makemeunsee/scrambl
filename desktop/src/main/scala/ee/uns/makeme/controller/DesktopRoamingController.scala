package ee.uns.makeme.controller

import models.roaming.RoamingModel
import hexatext.mvc.controller.{RoamingMenu, ScreenInputProcessor, DefaultScreenInputProcessor, RoamingController}
import rendering.screen.RoamingScreen
import com.badlogic.gdx.Input.Keys

class DesktopRoamingController(override val model: RoamingModel, override val screen: RoamingScreen)
    extends RoamingController(model, screen) {

  override val inputProcessor: ScreenInputProcessor = new DefaultScreenInputProcessor(screen, this)
                                                      with DesktopInputController {
    override def keyUp(key: Int): Boolean = {
      if (key == Keys.SPACE) {
        introCancel.cancelled = true
        true
      } else if (key == Keys.F1) {
        menu.toggleHelp()
        true
      } else
        super.keyUp(key)
    }
  }

  import widgets.skin
  override val menu = new RoamingMenu(this) {
    override protected def helpTextLines = super.helpTextLines ++ Seq(
      "",
      "Keyboard shortcuts:",
      "TAB: show/hide UI.",
      "SPACE / click during animation sequence: skip.",
      "F1: show/hide this help dialog.",
      "F8: show/hide FPS.",
      "F11: toggle fullscreen.",
      "F12: take screenshot.",
      "ESC: exit fullscreen or exit game.")
  }
}
