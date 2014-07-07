package world2d

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class HexaGridSpec extends FlatSpec with Matchers {
  import Hexagon.{NeighbourN, NeighbourNE, NeighbourNW, NeighbourS, NeighbourSE, NeighbourSW}
  
  "A HexaGrid" should "know what hexagnon lies where" in {
    
    val originH = Hexagon(0,0)
    
    HexaGrid.at(Point(0.3f, 0.3f))  should be (originH)
    HexaGrid.at(Point(-0.3f,0.3f))  should be (originH)
    HexaGrid.at(Point(0.3f, -0.3f)) should be (originH)
    HexaGrid.at(Point(-0.3f,-0.3f)) should be (originH)
    HexaGrid.at(Point(-0.99f,0))    should be (originH)
    HexaGrid.at(Point(0.99f,0))     should be (originH)
    
    HexaGrid.at(Point(1.4f, 0.8f))   should be (NeighbourNE(originH))
    HexaGrid.at(Point(1.6f, 0.9f))   should be (NeighbourNE(originH))
    HexaGrid.at(Point(1.4f, 0.9f))   should be (NeighbourNE(originH))
    HexaGrid.at(Point(1.6f, 0.8f))   should be (NeighbourNE(originH))
    HexaGrid.at(Point(0.51f, 0.86f)) should be (NeighbourNE(originH))
    HexaGrid.at(Point(1.49f, 0.86f)) should be (NeighbourNE(originH))
    HexaGrid.at(Point(1.4f,0))       should be (NeighbourNE(originH))
    
    HexaGrid.at(Point(-0.1f,1.6f))    should be (NeighbourN(originH))
    HexaGrid.at(Point(-0.1f,1.8f))    should be (NeighbourN(originH))
    HexaGrid.at(Point(0.1f, 1.8f))    should be (NeighbourN(originH))
    HexaGrid.at(Point(0.1f, 1.6f))    should be (NeighbourN(originH))
    HexaGrid.at(Point(-0.99f, 1.73f)) should be (NeighbourN(originH))
    HexaGrid.at(Point(0.99f, 1.73f))  should be (NeighbourN(originH))
    
    HexaGrid.at(Point(-1.4f, 0.8f))   should be (NeighbourNW(originH))
    HexaGrid.at(Point(-1.6f, 0.9f))   should be (NeighbourNW(originH))
    HexaGrid.at(Point(-1.4f, 0.9f))   should be (NeighbourNW(originH))
    HexaGrid.at(Point(-1.6f, 0.8f))   should be (NeighbourNW(originH))
    HexaGrid.at(Point(-0.51f, 0.86f)) should be (NeighbourNW(originH))
    HexaGrid.at(Point(-1.49f, 0.86f)) should be (NeighbourNW(originH))
    
    HexaGrid.at(Point(-1.4f, -0.8f))   should be (NeighbourSW(originH))
    HexaGrid.at(Point(-1.6f, -0.9f))   should be (NeighbourSW(originH))
    HexaGrid.at(Point(-1.4f, -0.9f))   should be (NeighbourSW(originH))
    HexaGrid.at(Point(-1.6f, -0.8f))   should be (NeighbourSW(originH))
    HexaGrid.at(Point(-0.51f, -0.86f)) should be (NeighbourSW(originH))
    HexaGrid.at(Point(-1.49f, -0.86f)) should be (NeighbourSW(originH))
    
    HexaGrid.at(Point(-0.1f,-1.6f))    should be (NeighbourS(originH))
    HexaGrid.at(Point(-0.1f,-1.8f))    should be (NeighbourS(originH))
    HexaGrid.at(Point(0.1f, -1.8f))    should be (NeighbourS(originH))
    HexaGrid.at(Point(0.1f, -1.6f))    should be (NeighbourS(originH))
    HexaGrid.at(Point(-0.99f, -1.73f)) should be (NeighbourS(originH))
    HexaGrid.at(Point(0.99f, -1.73f))  should be (NeighbourS(originH))
    
    HexaGrid.at(Point(1.4f, -0.8f))   should be (NeighbourSE(originH))
    HexaGrid.at(Point(1.6f, -0.9f))   should be (NeighbourSE(originH))
    HexaGrid.at(Point(1.4f, -0.9f))   should be (NeighbourSE(originH))
    HexaGrid.at(Point(1.6f, -0.8f))   should be (NeighbourSE(originH))
    HexaGrid.at(Point(0.51f, -0.86f)) should be (NeighbourSE(originH))
    HexaGrid.at(Point(1.49f, -0.86f)) should be (NeighbourSE(originH))
    
    HexaGrid.at(Point(1.4f, -2.4f))   should be (NeighbourS(NeighbourSE(originH)))
    HexaGrid.at(Point(1.6f, -2.4f))   should be (NeighbourS(NeighbourSE(originH)))
    HexaGrid.at(Point(1.4f, -2.7f))   should be (NeighbourS(NeighbourSE(originH)))
    HexaGrid.at(Point(1.6f, -2.7f))   should be (NeighbourS(NeighbourSE(originH)))
    HexaGrid.at(Point(0.51f, -2.59f)) should be (NeighbourS(NeighbourSE(originH)))
    HexaGrid.at(Point(1.49f, -2.59f)) should be (NeighbourS(NeighbourSE(originH)))
  }
  
  it should "create centered, symetric flakes" in {
    //TODO
  }

  it should "find paths" in {
    //TODO
  }
  
  it should "list hexagons in a window" in {
    HexaGrid.window(Point(0,0), Point(0,0)) should contain (Hexagon(0,0))
    HexaGrid.window(Point(0,0), Point(0.5f,0.5f)) should contain (Hexagon(0,0))
    
    val topRightQuadrant = HexaGrid.window(Point(0,0), Point(1.4f, 0.8f))
    topRightQuadrant should contain (Hexagon(0,0))
    topRightQuadrant should contain (Hexagon(0,1))
    topRightQuadrant should contain (Hexagon(1,1))
    
    val bottomRightQuadrant = HexaGrid.window(Point(0,0), Point(1.4f, -0.8f))
    bottomRightQuadrant should contain (Hexagon(0,0))
    bottomRightQuadrant should contain (Hexagon(0,-1))
    bottomRightQuadrant should contain (Hexagon(1,0))
    
    val bottomLeftQuadrant = HexaGrid.window(Point(0,0), Point(-1.4f, -0.8f))
    bottomLeftQuadrant should contain (Hexagon(0,0))
    bottomLeftQuadrant should contain (Hexagon(0,-1))
    bottomLeftQuadrant should contain (Hexagon(-1,0))
    
    val topLeftQuadrant = HexaGrid.window(Point(0,0), Point(-1.4f, 0.8f))
    topLeftQuadrant should contain (Hexagon(0,0))
    topLeftQuadrant should contain (Hexagon(0,1))
    topLeftQuadrant should contain (Hexagon(-1,1))
    
    val allNeighbours = HexaGrid.window(Point(-0.75f, 1.3f), Point(0.75f, -1.3f))
    allNeighbours should contain (Hexagon(0,0))
    allNeighbours should contain (Hexagon(0,-1))
    allNeighbours should contain (Hexagon(-1,0))
    allNeighbours should contain (Hexagon(-1,1))
    allNeighbours should contain (Hexagon(1,1))
    allNeighbours should contain (Hexagon(0,1))
  }
}