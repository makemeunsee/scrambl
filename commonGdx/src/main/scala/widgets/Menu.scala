package widgets

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.badlogic.gdx.scenes.scene2d.ui.Skin

trait ResizableStage extends Stage {
  def resize(width: Int, height: Int) {
    getViewport.setWorldSize(width, height)
    getViewport.update(width, height, true)
  }
}

trait Menu extends ResizableStage {
  def showMenuAction: Stage
  def toggle(): Unit
}

class DefaultMenu(implicit skin: Skin) extends Menu { menu =>
  private var active = false
  
  def toggle() {
    active = !active
    menu.getActors.toArray foreach { a =>
      a.setVisible(active)
    }
  }
  
  def isActive = active
    
  val showMenuAction = new Stage {
    override def keyUp(key: Int): Boolean = {
      if ( key == Keys.TAB ) {
        toggle()
        true
      } else {
        false
      }
    }
  }
  
  override def addActor(actor: Actor) {
    super.addActor(actor)
    actor.setVisible(isActive)
  }
}