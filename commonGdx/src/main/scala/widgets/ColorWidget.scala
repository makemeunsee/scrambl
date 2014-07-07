package widgets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.graphics.Color
import rendering.shaders.GdxColor

class ColorWidget(implicit val skin: Skin) extends Table { widget =>

  setBackground(skin.getDrawable("menu_button"))
  
  private val colorView = createColorGroup

  add(colorView)

  private val channels = (0 to 3) map (_ => createSlider)
  
  channels foreach addSlider
  
  row()
  
  add()
  add(new Label("R", skin))
  add(new Label("G", skin))
  add(new Label("B", skin))
  add(new Label("A", skin))

  def updateColor(color: rendering.Color) {
    colorView.setColor(new Color(color.r, color.g, color.b, color.a))
    channels(0).setValueSilently(color.r)
    channels(1).setValueSilently(color.g)
    channels(2).setValueSilently(color.b)
    channels(3).setValueSilently(color.a)
  }

  def getViewColor = new GdxColor(colorView.getColor)
  
  private def createColorGroup = {
    val colimg = new Image(new Texture(colorPixmap(stdWidth, stdHeight, new Color(0xFFFFFFFF))))
    val group = new Table {
      override def getColor = colimg.getColor
      override def setColor(color: Color) {
        colimg.setColor(color)
      }
    }
    group.addActor(new Image(skin, "checkered"))
    group.add(colimg)
    group
  }
  
  private def createSlider = {
    val slider = new SilentSlider(0, 1, 0.05f, true)
    slider.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        updateColorView()
      }
    })
    slider
  }

  private def addSlider(slider: Slider) = {
    add(slider).width(16).height(stdHeight*2)
  }

  private def updateColorView() {
    colorView.setColor(new Color(channels(0).getValue, channels(1).getValue, channels(2).getValue, channels(3).getValue))
  }
}

