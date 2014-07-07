package ee.uns.makeme.launchers

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import ee.uns.makeme.cfg.DesktopConfig
import game.{BenchmarkGame, BenchmarkGame2}

object Benchmark extends App {
  new LwjglApplication(new BenchmarkGame2, new DesktopConfig)
}