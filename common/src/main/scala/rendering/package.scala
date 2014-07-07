package object rendering {
  def colorToFloats(color: Color): (Float, Float, Float) = {
    (color.r, color.g , color.b)
  }
}