package ee.uns.makeme

import org.robovm.cocoatouch.foundation.NSAutoreleasePool
import org.robovm.cocoatouch.uikit.UIApplication

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

class Main extends IOSApplication.Delegate {
  override protected def createApplication(): IOSApplication = {
    val config = new IOSApplicationConfiguration
    new IOSApplication(new Scrambl, config)
  }
}

object Main {
  def main(args: Array[String]) {
      val pool = new NSAutoreleasePool
      UIApplication.main(args, null, classOf[Main])
      pool.drain()
  }
}
