package widgets

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class RollingLabel(firstLine: String, skin: Skin, lineCount: Int = 3) extends Label(firstLine, skin) {
  
  private var lines = Seq(firstLine)
  setText(lines.mkString("\n"))
  
  def addLine(newLine: String) {
    if (lines.size < lineCount) lines = lines :+ newLine
    else lines = lines.tail :+ newLine
    setText(lines.mkString("\n"))
  }
  
  def clearLines() {
    lines = Seq.empty
    setText("")
  }

}