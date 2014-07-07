package world2d

import org.scalatest.{Matchers, FlatSpec}
import world2d.Hexagon.{NeighbourN => N, NeighbourNE => NE, NeighbourNW => NW, NeighbourS => S, NeighbourSE => SE, NeighbourSW => SW}
import world2d.Hexagon.{NeighbourN => N}
import world2d.Hexagon.{NeighbourNE => NE}
import world2d.Hexagon.{NeighbourNW => NW}
import world2d.Hexagon.{NeighbourS => S}
import world2d.Hexagon.{NeighbourSE => SE}
import world2d.Hexagon.{NeighbourSW => SW}


class LivingHexagonSpec extends FlatSpec with Matchers {

  val hexa0 = new LivingHexagon(0,0)

  val hN  = new LivingHexagon(N (hexa0), 0, 0)
  val hNW = new LivingHexagon(NW(hexa0), 0, 0)
  val hNE = new LivingHexagon(NE(hexa0), 0, 0)
  val hSE = new LivingHexagon(SE(hexa0), 0, 0)
  val hSW = new LivingHexagon(SW(hexa0), 0, 0)
  val hS  = new LivingHexagon(S (hexa0), 0, 0)

  "A living hexagon" should "give sextants" in {
    hexa0.sextant(hN.center).get should be (N)
    hexa0.sextant(hNE.center).get should be (NE)
    hexa0.sextant(hSE.center).get should be (SE)
    hexa0.sextant(hS.center).get should be (S)
    hexa0.sextant(hSW.center).get should be (SW)
    hexa0.sextant(hNW.center).get should be (NW)

    hN.sextant(hexa0.center).get should be (S)
    hNE.sextant(hexa0.center).get should be (SW)
    hSE.sextant(hexa0.center).get should be (NW)
    hS.sextant(hexa0.center).get should be (N)
    hSW.sextant(hexa0.center).get should be (NE)
    hNW.sextant(hexa0.center).get should be (SE)

    hNW.sextant(new LivingHexagon(NW(hNW), 0, 0).center).get should be (NW)
    hNW.sextant(new LivingHexagon(SW(hNW), 0, 0).center).get should be (SW)
    hNW.sextant(new LivingHexagon(N (hNW), 0, 0).center).get should be (N)
    hNW.sextant(new LivingHexagon(NE(hNW), 0, 0).center).get should be (NE)
    hNW.sextant(new LivingHexagon(SE(hNW), 0, 0).center).get should be (SE)
    hNW.sextant(new LivingHexagon(S (hNW), 0, 0).center).get should be (S)

  }

}
