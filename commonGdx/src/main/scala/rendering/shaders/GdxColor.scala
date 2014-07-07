package rendering.shaders

import com.badlogic.gdx.graphics.Color

class GdxColor(val gdxCol: Color) extends rendering.Color {
  def this(rgba: Int) = this(new Color(rgba))
  def this(r: Float, g: Float, b: Float, a: Float) = this(new Color(r, g, b, a))
                             
  val r: Float = gdxCol.r
  val g: Float = gdxCol.g
  val b: Float = gdxCol.b
  val a: Float = gdxCol.a
  
  def mul(_r: Float, _g: Float, _b: Float, _a: Float): GdxColor = new GdxColor(gdxCol.cpy.mul(_r, _g, _b, _a))
  def add(_r: Float, _g: Float, _b: Float, _a: Float): GdxColor = new GdxColor(gdxCol.cpy.add(_r, _g, _b, _a))
  def sub(_r: Float, _g: Float, _b: Float, _a: Float): GdxColor = new GdxColor(gdxCol.cpy.sub(_r, _g, _b, _a))
  
  def toFloatBits = gdxCol.toFloatBits

  private def fToHex(f: Float) = {
    val s = Integer.toHexString((255*f).toInt)
    if (s.length < 2) s"0$s"
    else s
  } 
  override def toString = s"0x${fToHex(r)}${fToHex(g)}${fToHex(b)}${fToHex(a)}"
}