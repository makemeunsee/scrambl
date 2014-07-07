package ee.uns.makeme.controller

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.InputProcessor
import rendering.screen.DefaultScreen

trait DesktopInputController extends InputProcessor {
  
  def screen: DefaultScreen[_]

  private val originalWidth = Gdx.graphics.getWidth
  private val originalHeight = Gdx.graphics.getHeight
  private val desktopMode = Gdx.graphics.getDesktopDisplayMode

  //Gdx.graphics.setDisplayMode(desktopMode)

  abstract override def keyUp(key: Int): Boolean = {
    if ( key == Keys.F8 ) {
      screen.showFps = !screen.showFps
      true
    } else if ( key == Keys.F11 ) {
      if ( Gdx.graphics.isFullscreen ) Gdx.graphics.setDisplayMode(originalWidth, originalHeight, false)
      else Gdx.graphics.setDisplayMode(desktopMode)
      true
    } else if ( key == Keys.F12 ) {
      val pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
      val filename = s"screen_${System.currentTimeMillis}."
      PixmapIO.writePNG(new FileHandle(filename+"png"), pixmap)
      pixmap.dispose()
      true
    } else if ( key == Keys.ESCAPE ) {
      exitFullscreenOrExit()
      true
    } else {
      super.keyUp(key)
    }
  }

  protected def exitFullscreenOrExit() {
    if ( Gdx.graphics.isFullscreen ) Gdx.graphics.setDisplayMode(originalWidth, originalHeight, false)
    else Gdx.app.exit()
  }
}