package world2d

object Point {
  val pattern = "^([-]{0,1}[0-9]+\\.[0-9]+),([-]{0,1}[0-9]+\\.[0-9]+)$".r
    
  def apply(str: String): Option[Point] = {
    val matches = pattern.findAllIn(str)
    if (matches.hasNext) Some(Point(matches.group(1).toFloat, matches.group(2).toFloat))
    else None
  }

  def apply[T](x: T, y: T)(implicit num: Numeric[T]): Point = {
    import num._
    Point(x.toFloat(), y.toFloat())
  }
}

case class Point(x: Float, y: Float) {
  def dot(other: Point): Float = x * other.x + y * other.y
  def *(scalar: Float): Point = copy(x * scalar, y * scalar)
  def /(scalar: Float): Point = copy(x / scalar, y / scalar)
  def +(other: Point): Point = copy(x + other.x, y + other.y)
  def -(other: Point): Point = copy(x - other.x, y - other.y)
  def unary_- = copy(-x, -y)
}