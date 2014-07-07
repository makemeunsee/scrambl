package world2d

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class PointSpec extends FlatSpec with Matchers {

  "A point" should "have coordinates" in {
    val p = Point(15f, 64.555f)
    p.x should be (15)
    p.y should be (64.555f)
  }
  
  it should "be scalable" in {
    val p = Point(1f, 2f)
    p * 3 should be (Point(3,6))
    p / -2 should be (Point(-0.5f,-1))
  }
  
  it should "be addable to another point" in {
    val p1 = Point(0f, -2f)
    val p2 = Point(10f, 2f)
    p1 + p2 should be (Point(10,0))
    val p3 = Point(5f, 1.5f)
    val p4 = Point(500f, 0.1f)
    p3 - p4 should be (Point(-495,1.4f))
  }
  
  it should "be negatable" in {
    val p = Point(1/3, 4*.5f)
    -p should be (Point(-1/3,-2))
  }
  
  it should "support dot product" in {
    val p1 = Point(0f, -2f)
    val p2 = Point(10f, 2f)
    p1 dot p2 should be (-4)
    val p3 = Point(-2f, 2f)
    val p4 = Point(2f, 2f)
    p3 dot p4 should be (0)
    val p5 = Point(1f, 2f)
    val p6 = Point(3f, 4f)
    p5 dot p6 should be (11)
  }
  
  it can "be built from strings and numbers" in {
    val p1_? = Point("1324132,12312")
    p1_? should be (None)
    val p2_? = Point("1324.132,12312")
    p2_? should be (None)
    val p3_? = Point("1324132.0 12312.0")
    p3_? should be (None)
    val p4_? = Point("123456789.0,987654321.0")
    p4_?.get should be (Point(123456789,987654321))
    val p5_? = Point("-1234.56789,987654321.0")
    p5_?.get should be (Point(-1234.56789f,987654321))
    val p6_? = Point("132413.2,-0.12312")
    p6_?.get should be (Point(132413.2f,-0.12312f))
    
    val p7 = Point(5d,1l)
    p7 should be (Point(5f,1f))
    val s: Short = 8
    val b: Byte = -5
    val p8 = Point(s,b)
    p8 should be (Point(8,-5))
  }
}