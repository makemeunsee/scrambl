import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.{Button, Skin}
import Button.ButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.graphics.{Pixmap, Color}

package object widgets {

  implicit val skin = new Skin(Gdx.files.internal("icons/buttons.json"))

  def button(prefix: String, action: => Unit, withDown: Boolean = true)(implicit skin: Skin) = {
    val buttonStyle = new ButtonStyle
    buttonStyle.up = skin.getDrawable(s"${prefix}_button")
    if (withDown) buttonStyle.down = skin.getDrawable(s"${prefix}_button_48_down")
    val button = new Button(buttonStyle)
    button.addListener(new ClickListener(0){
      override def clicked(e: InputEvent, x: Float, y: Float) {
        action
      }
    })
    button
  }
  
  val (stdWidth, stdHeight) = (48, 48)
  
  def blackPixmap(width: Int, height: Int) = new Pixmap(width, height, Pixmap.Format.RGBA8888)
  
  def colorPixmap(width: Int, height: Int, color: Color) = {
    val res = blackPixmap(width, height)
    res.setColor(color)
    res.fill()
    res
  }
}