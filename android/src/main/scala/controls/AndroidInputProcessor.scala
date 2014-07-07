package controls

import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx. InputProcessor
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import models.roaming.RoamingModel
import rendering.screen.{RoamingScreen, ScramblScreen}
import com.badlogic.gdx.math.Vector2
import hexatext.mvc.controller.{StdController, DefaultScreenInputProcessor, RoamingController, RoamingMenu}

class AndroidGestureListener(controller: StdController,
                             val screen: ScramblScreen)
  extends GestureListener { gesture =>

  private val parentProcessor = new DefaultScreenInputProcessor(screen, controller)

  val postZoomDragDelay = 200l
  var lastZoom = 0l

  def fling(velocityX: Float, velocityY: Float, button: Int) = false

  def longPress(x: Float, y: Float) = {
    controller.menu.toggle()
    true
  }

  def pan(x: Float, y: Float, deltaX: Float, deltaY: Float) = {
//    println(s"pan $x, $y, $deltaX, $deltaY")
    if (System.currentTimeMillis > lastZoom + postZoomDragDelay) {
      parentProcessor.touchDragged(x.toInt, y.toInt, 0)
    }
    false
  }

  def panStop(x: Float, y: Float, pointer: Int, button: Int) = {
//    println(s"panStop $x, $y, $pointer, $button")
    false
  }

  def pinch(initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2) = {
    // center vertical growing 04-24 22:30:57.229: I/System.out(21041): pinch [107.0:951.0], [1033.0:1011.0], [370.0:979.0], [786.0:981.0]
    // center horizontal shrinking 04-24 22:31:46.319: I/System.out(21041): pinch [585.0:213.0], [592.0:1698.0], [571.0:648.0], [594.0:1236.0]

//    println(s"pinch $initialPointer1, $initialPointer2, $pointer1, $pointer2")
    parentProcessor.mouseMoved((initialPointer1.x/2f + initialPointer2.x/2f).toInt, (initialPointer1.y/2f + initialPointer2.y/2f).toInt)
    false
  }

  def tap(x: Float, y: Float, count: Int, button: Int) = {
    // top left 04-24 22:27:06.069: I/System.out(21041): tap 1081.0, 93.0, 1, 0
    val (xi, yi) = (x.toInt, y.toInt)
    parentProcessor.touchDown(xi, yi, 0, button)
    parentProcessor.touchUp(xi, yi, 0, button)
//    println(s"tap $x, $y, $count, $button")
    false
  }

  def touchDown(x: Float, y: Float, pointer: Int, button: Int) = {
    // top left 04-24 22:27:05.929: I/System.out(21041): touchDown 1083.0, 93.0, 0, 0
    // bottom right 04-24 22:28:10.829: I/System.out(21041): touchDown 62.0, 1792.0, 0, 0
  	// center 04-24 22:28:51.889: I/System.out(21041): touchDown 550.0, 925.0, 0, 0
  	parentProcessor.mouseMoved(x.toInt, y.toInt)
//  	println(s"touchDown $x, $y, $pointer, $button")
    false
  }

  // memory of the initial camera zoom at the start of zooming
  var initialScale = (1f, Float.NegativeInfinity)

  def zoom(initialDistance: Float, distance: Float) = {
    // center vertical growing 04-24 22:30:57.229: I/System.out(21041): zoom 927.94183, 416.00482
  	// center horizontal shrinking 04-24 22:31:46.319: I/System.out(21041): zoom 1485.0165, 588.44965
//    println(s"zoom $initialDistance, $distance")
    lastZoom = System.currentTimeMillis
    // memorize the camera zoom value at zoom start
    // if the initialDistance changed, it means a new zoom is starting
    if (initialScale._2 != initialDistance) {
      initialScale = (screen.camera.zoom, initialDistance)
    }
    val newZoom = initialScale._1 / distance * initialDistance
    if (newZoom > screen.zoomMax || newZoom < screen.zoomMin )
      false
    else {
      screen.setZoom(newZoom)
      true
    }
  }
}

class AndroidInputProcessor(model: RoamingModel,
                            screen: RoamingScreen)
    extends RoamingController(model, screen) {

  override val inputProcessor: InputProcessor = new GestureDetector(new AndroidGestureListener(this, screen))

  import widgets.skin
  override val menu = new RoamingMenu(this) {
    override protected def helpTextLines = super.helpTextLines ++ Seq(
      "",
      "Tap screen during animation sequence to skip.",
      "Long press to toggle the UI.")

    helpButton.setStyle(new ButtonStyle(helpButton.getStyle) { style =>
      style.up = skin.getDrawable("help_button_bigger")
    })

    applyBiggerFont(console)
    applyBiggerFont(scoreLabel)
    applyBiggerFont(helpLabel)
    applyBiggerFont(qButton)
    applyBiggerFont(nlButton)
//    applyBiggerFont(krButton)

    override protected def consoleSize = (1000, 130)
    override protected def nlButtonSize = (240, 60)
//    override protected def krButtonSize = (240, 60)
    override protected def qButtonSize = (240, 60)
    override protected def helpTextLineHeight = 45
    override protected def helpTextWidth = 1080
  }

  private def applyBiggerFont(label: Label) {
    label.setStyle(new LabelStyle(label.getStyle) { style =>
      style.font = skin.getFont("bigger-font")
    })
  }

  private def applyBiggerFont(textButton: TextButton) {
    textButton.setStyle(new TextButtonStyle(textButton.getStyle) { style =>
      style.font = skin.getFont("bigger-font")
    })
  }

}