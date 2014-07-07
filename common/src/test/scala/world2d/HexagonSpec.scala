package world2d

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import Hexagon.{NeighbourN => N, NeighbourNE => NE, NeighbourNW => NW, NeighbourS => S, NeighbourSE => SE, NeighbourSW => SW}
    
class HexagonSpec extends FlatSpec with Matchers {
  
  val hexa0 = Hexagon(0, 0)
  val hexa1 = Hexagon(36, -17)
  val hexa2 = Hexagon(-355, 65536)
  val hexa1Center = Point(36*Hexagon.xSpacing, -17*Hexagon.ySpacing)
  val hexa2Center = Point(-355*Hexagon.xSpacing, 65536*Hexagon.ySpacing - Hexagon.ySpacing / 2)
  
  "A hexagon" should "have a center and points" in {
    hexa0.center should be (Point(0,0))
    hexa0.points should be (hexa0.points)
    hexa1.center should be (hexa1Center)
    hexa1.points should be (hexa0.points.map(_ + hexa1Center))
    hexa2.center should be (hexa2Center)
    hexa2.points should be (hexa0.points.map(_ + hexa2Center))
  }
  
  it should "build from a string" in {
    val p1_? = Hexagon("1324132,12312")
    p1_?.get should be (Hexagon(1324132,12312))
    val p2_? = Hexagon("1324.132,12312")
    p2_? should be (None)
    val p3_? = Hexagon("1324132.0 12312.0")
    p3_? should be (None)
    val p4_? = Hexagon("123456789,987654321")
    p4_?.get should be (Hexagon(123456789,987654321))
    val p5_? = Hexagon("-1234,987654321")
    p5_?.get should be (Hexagon(-1234,987654321))
    val p6_? = Hexagon("0,-012")
    p6_?.get should be (Hexagon(0,-12))
  }
  
  it should "have neighbours" in {
    N (hexa0) should be (Hexagon(0, 1))
    NE(hexa0) should be (Hexagon(1, 1))
    NW(hexa0) should be (Hexagon(-1, 1))
    S (hexa0) should be (Hexagon(0, -1))
    SW(hexa0) should be (Hexagon(-1, 0))
    SE(hexa0) should be (Hexagon(1, 0))
    
    N (hexa1) should be (Hexagon(36, -16))
    NE(hexa1) should be (Hexagon(37, -16))
    NW(hexa1) should be (Hexagon(35, -16))
    S (hexa1) should be (Hexagon(36, -18))
    SW(hexa1) should be (Hexagon(35, -17))
    SE(hexa1) should be (Hexagon(37, -17))
    
    N (hexa2) should be (Hexagon(-355, 65537))
    NE(hexa2) should be (Hexagon(-354, 65536))
    NW(hexa2) should be (Hexagon(-356, 65536))
    S (hexa2) should be (Hexagon(-355, 65535))
    SW(hexa2) should be (Hexagon(-356, 65535))
    SE(hexa2) should be (Hexagon(-354, 65535))
  }
  
  it should "have consistent neighbours" in {
    Seq(hexa0, hexa1, hexa2) foreach { h =>
      S( N (h)) should be (h)
      SW(NE(h)) should be (h)
      SE(NW(h)) should be (h)
      N( S (h)) should be (h)
      NE(SW(h)) should be (h)
      NW(SE(h)) should be (h)
    }
  }
  
  it should "provide its symmetrical around (0,0)" in {
    hexa0.symmetrical should be (hexa0)
    
    N (hexa0).symmetrical should be (Hexagon(0, -1))
    NE(hexa0).symmetrical should be (Hexagon(-1, 0))
    NW(hexa0).symmetrical should be (Hexagon(1, 0))
    S (hexa0).symmetrical should be (Hexagon(0, 1))
    SW(hexa0).symmetrical should be (Hexagon(1, 1))
    SE(hexa0).symmetrical should be (Hexagon(-1, 1))
    
    NW(N (hexa0)).symmetrical should be (Hexagon(1, -1))
    N( N (hexa0)).symmetrical should be (Hexagon(0, -2))
    NE(N (hexa0)).symmetrical should be (Hexagon(-1, -1))
    NE(NE(hexa0)).symmetrical should be (Hexagon(-2, -1))
    SE(NE(hexa0)).symmetrical should be (Hexagon(-2, 0))
    SE(SE(hexa0)).symmetrical should be (Hexagon(-2, 1))
    SW(S (hexa0)).symmetrical should be (Hexagon(1, 2))
    S( S (hexa0)).symmetrical should be (Hexagon(0, 2))
    SE(S (hexa0)).symmetrical should be (Hexagon(-1, 2))
    NW(NW(hexa0)).symmetrical should be (Hexagon(2, -1))
    SW(NW(hexa0)).symmetrical should be (Hexagon(2, 0))
    SW(SW(hexa0)).symmetrical should be (Hexagon(2, 1))
  }

  it can "be rotated if asked nicely" in {
    val h1 = Hexagon(0, 1)
    val h2 = Hexagon(-4, 4)
    val h3 = Hexagon(5, 13)

    // rotate around self return self
    (-10 to 10)
    .map { i =>
      hexa0.rotation(hexa0, i) should be (hexa0)
      h1.rotation(h1, i) should be (h1)
      h2.rotation(h2, i) should be (h2)
      h3.rotation(h3, i) should be (h3)
    }

    // return 0 times return self
    h1.rotation(hexa0, 0) should be (h1)
    h2.rotation(h1, 0) should be (h2)
    h3.rotation(h2, 0) should be (h3)
    hexa0.rotation(h3, 0) should be (hexa0)

    // actual rotations
    h1.rotation(hexa0, 1) should be (NW(hexa0))
    h1.rotation(hexa0, 2) should be (SW(hexa0))
    h1.rotation(hexa0, 3) should be (S (hexa0))
    h1.rotation(hexa0, 4) should be (SE(hexa0))
    h1.rotation(hexa0, 5) should be (NE(hexa0))
    h1.rotation(hexa0, 6) should be (N (hexa0))

    h1.rotation(hexa0, -1) should be (NE(hexa0))
    h1.rotation(hexa0, -2) should be (SE(hexa0))
    h1.rotation(hexa0, -3) should be (S (hexa0))
    h1.rotation(hexa0, -4) should be (SW(hexa0))
    h1.rotation(hexa0, -5) should be (NW(hexa0))
    h1.rotation(hexa0, -6) should be (N (hexa0))
  }

  it should "behave like the good little sextant it is" in {
    hexa0.sextant(N (hexa0).center).get should be (N)
    hexa0.sextant(NE(hexa0).center).get should be (NE)
    hexa0.sextant(SE(hexa0).center).get should be (SE)
    hexa0.sextant(S (hexa0).center).get should be (S)
    hexa0.sextant(SW(hexa0).center).get should be (SW)
    hexa0.sextant(NW(hexa0).center).get should be (NW)

    N (hexa0).sextant(hexa0.center).get should be (S)
    NE(hexa0).sextant(hexa0.center).get should be (SW)
    SE(hexa0).sextant(hexa0.center).get should be (NW)
    S (hexa0).sextant(hexa0.center).get should be (N)
    SW(hexa0).sextant(hexa0.center).get should be (NE)
    NW(hexa0).sextant(hexa0.center).get should be (SE)
  }

  "Hexagons" can "be read from files" in {
    Hexagon.fromFile("logo") should not be Seq.empty
  }
  
  "Routes" should "exist between 2 hexagons" in {
    Seq(hexa0, hexa1, hexa2) foreach { h =>
      h.routeTo(h) should be (Seq.empty)
      h(h.routeTo(N (N (h)))) should be (N (N (h)))
      h(h.routeTo(S (S (h)))) should be (S (S (h)))
      h(h.routeTo(NE(NE(h)))) should be (NE(NE(h)))
      h(h.routeTo(NW(NW(h)))) should be (NW(NW(h)))
      h(h.routeTo(SE(SE(h)))) should be (SE(SE(h)))
      h(h.routeTo(SW(SW(h)))) should be (SW(SW(h)))
    }
  }
  
  they should "be reversable and consistent" in {
    val rA = hexa0.routeTo(hexa1)
    val rB = hexa0.routeFrom(hexa1)
    hexa0(rA) should be (hexa1)
    hexa1(rB) should be (hexa0)
    hexa1(Hexagon.reverseRoute(rA)) should be (hexa0)
    hexa0(Hexagon.reverseRoute(rB)) should be (hexa1)
    
    // hex2 too far for practical route tests
  }
}