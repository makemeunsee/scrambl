package ee.uns.makeme.cfg

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

class DesktopConfig extends LwjglApplicationConfiguration { cfg =>
  cfg.title = "scrambl"
  cfg.height = 1024
  cfg.width = 1024
  cfg.forceExit = true
  cfg.vSyncEnabled = false
  cfg.backgroundFPS = 0
  cfg.foregroundFPS = 0
}