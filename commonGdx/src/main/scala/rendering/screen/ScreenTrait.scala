package rendering.screen

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.Gdx
import world2d.Hexagon
import world2d.Point
import views.View

trait ScreenTrait[H <: Hexagon] extends View[H] {
  protected def worldHexas: (Seq[H], H => Int)
  def camera: Camera
  def zoom: Float
  def setZoom(newZoomValue: Float): Unit

  def viewToWorldCoordinates(x: Int, y: Int): Point = {
    Point(x - Gdx.graphics.getWidth/2, Gdx.graphics.getHeight/2 - y) * zoom + Point(camera.position.x, camera.position.y)
  }
}