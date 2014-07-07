package widgets

import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class SilentSlider(min: Float, max: Float, step: Float, vertical: Boolean)(implicit val skin: Skin)
    extends Slider(min, max, step, vertical, skin, if (vertical) "default-vertical" else "default-horizontal") {
  
  private var allowFire = true

  override def setValue(newValue: Float) = {
    if (math.abs(newValue - getValue) >= getStepSize) super.setValue(newValue)
    else true
  }

  def setValueSilently(newValue: Float) = {
    allowFire = false
    setValue(newValue)
    allowFire = true
  }

  override def fire(event: Event) = {
    if (allowFire) super.fire(event)
    else false
  }
}