package rendering

trait Color {
  def r: Float
  def g: Float
  def b: Float
  def a: Float
  def mul(_r: Float, _g: Float, _b: Float, _a: Float): Color
  def add(_r: Float, _g: Float, _b: Float, _a: Float): Color
  def sub(_r: Float, _g: Float, _b: Float, _a: Float): Color
  def toFloatBits: Float
}