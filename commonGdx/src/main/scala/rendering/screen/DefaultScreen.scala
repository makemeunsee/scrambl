package rendering.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import world2d.Hexagon
import world2d.Point
import com.badlogic.gdx.graphics.g2d.{SpriteBatch, BitmapFont}

object DefaultScreen {
  val (maxWidth, maxHeight) = {
    val ddm = Gdx.graphics.getDesktopDisplayMode
    (ddm.width.toFloat, ddm.height.toFloat)
  }
  val xScreenMax = maxWidth / 2f
  val yScreenMax = maxHeight / 2f
  val viewRatio = maxWidth / maxHeight
}

import DefaultScreen._

abstract class DefaultScreen[H <: Hexagon] extends Screen with ScreenTrait[H] {
  
  private val (worldWidth, worldHeight) = model.worldSize
  
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  val zoomMax = math.max(worldWidth / maxWidth, worldHeight / maxHeight)
  val zoomMin = zoomMax / 8
  private val zoomSpeed = 1.2f
  camera.zoom = zoomMax / 2
  
  protected val worldHexas = /*perf.perfed("world hexas")*/ {
    // extends worlds hexas from model to match view ratio
    // ensures at least the world is displayable on screen
    val worldRatio = worldWidth / worldHeight
    val viewWidth = if (worldRatio < viewRatio) worldWidth * viewRatio / worldRatio else worldWidth
    val viewHeight = if (worldRatio < viewRatio) worldHeight else worldHeight / viewRatio * worldRatio
    model.window(Point(-viewWidth/2, -viewHeight/2), Point(viewWidth/2, viewHeight/2))
  }
  
  validateMove()
  
  def zoom = camera.zoom
  
  def zoomIn() {
    camera.zoom /= zoomSpeed
    validateZoom()
  }
  def zoomOut() {
    camera.zoom *= zoomSpeed
    validateZoom()
  }
  def setZoom(newZoomValue: Float) {
    camera.zoom = newZoomValue
    validateZoom()
  }
  
  private val (xMax, yMax) = (xScreenMax * zoomMax, yScreenMax * zoomMax)
  private val (xMin, yMin) = (-xMax, -yMax)
  private val (xSpan, ySpan) = (2 * xMax, 2 * yMax)
  
  // properties of the current view (zoom and viewport dependent, recomputed only when needed)
  private var viewWidth = 0f
  private var halfViewWidth = 0f
  private var viewHeight = 0f
  private var halfViewHeight = 0f
  private def updateViewSize() {
    val farPlanePoints = camera.frustum.planePoints.take(4)
    viewWidth = farPlanePoints(1).x - farPlanePoints(0).x
    halfViewWidth = viewWidth / 2f
    viewHeight = farPlanePoints(2).y - farPlanePoints(0).y
    halfViewHeight = viewHeight / 2f
  }
  updateViewSize()
    
  private def validateZoom() {
    // validate zoom
    if ( camera.zoom > zoomMax ) {
      camera.zoom = zoomMax
    } else if ( camera.zoom < zoomMin ) {
      camera.zoom = zoomMin
    }
    camera.update()
    updateViewSize()
    validateMove()
  }
  
  def moveCam(deltaX: Float, deltaY: Float) {
    camera.position.set(camera.position.x + deltaX*camera.zoom, camera.position.y + deltaY*camera.zoom, camera.position.z)
    validateMove()
  }
  
  private def updateCam(w: Float = Gdx.graphics.getWidth,
      h: Float = Gdx.graphics.getHeight) {
    camera.viewportWidth = w
    camera.viewportHeight = h
    camera.update()
    updateViewSize()
    validateMove()
  }
  
  private def validateMove() {
    // moving does not change frustum size, just its position
    // so dont update cam just yet
    if ( viewWidth > xSpan ) {
      camera.position.x = 0
    } else if ( camera.position.x + halfViewWidth > xMax ) {
      camera.position.x = xMax - halfViewWidth
    } else if ( camera.position.x - halfViewWidth < xMin ) {
      camera.position.x = xMin + halfViewWidth
    }
    if ( viewHeight > ySpan ) {
      camera.position.y = 0
    } else if ( camera.position.y + halfViewHeight > yMax ) {
      camera.position.y = yMax - halfViewHeight
    } else if ( camera.position.y - halfViewHeight < yMin ) {
      camera.position.y = yMin + halfViewHeight
    }
    camera.update()
  }

  // fps text
  var showFps = false
  private val font = new BitmapFont(Gdx.files.internal("fonts/ubuntu_mono_16.fnt"))
  private val textBatch = new SpriteBatch

  protected def renderHud(delta: Float, now: Float) {
    if (showFps) {
      textBatch.begin()
      font.draw(textBatch, s"${Gdx.graphics.getFramesPerSecond}", 30, 30)
      textBatch.end()
    }
  }

  def render(delta: Float)
  
  def resize(width: Int, height: Int) {
    updateCam()
    val textCam = new OrthographicCamera(width, height)
    textCam.translate(width/2, height/2)
    textCam.update()
    textBatch.setProjectionMatrix(textCam.combined)
  }
  
  def show() = {}
  def hide() = {}
  def pause() = {}
  def resume() = {}
  def dispose() = {}
}