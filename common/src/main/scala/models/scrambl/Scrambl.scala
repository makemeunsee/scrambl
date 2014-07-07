package models.scrambl

import world2d.Hexagon

case class ScramblPattern(iv: Set[Hexagon], sig: Set[Hexagon]) {
  def fuseTim = iv.size
  def exploded = iv.map(_.neighbours.toSet).reduce((s1, s2) => s1 ++ s2 -- (s1 intersect s2))
}

case class Scrambl(origin: Hexagon, pattern: ScramblPattern) {
  def exploded = {
    val route = origin.routeFrom(Hexagon(0,0))
    pattern.exploded map { h => h(route) }
  }
}

object Scrambl {
  val scrambl1 = ScramblPattern(Set(Hexagon(0,0)), Set(Hexagon(0,0)))
  
  val scrambl2ai = ScramblPattern(Set(Hexagon(1,1),
                              Hexagon(-1,0)),
                          Set(Hexagon(1,1),
                              Hexagon(-1,0)))
                              
  val scrambl2aj = ScramblPattern(Set(Hexagon(-1,1),
                              Hexagon(1,0)),
                          Set(Hexagon(-1,1),
                              Hexagon(1,0)))
                              
  val scrambl2ak = ScramblPattern(Set(Hexagon(0,1),
                              Hexagon(0,-1)),
                          Set(Hexagon(0,1),
                              Hexagon(0,-1)))
                              
  val scrambl3ai = ScramblPattern(Set(Hexagon(1,2),
                              Hexagon(1,-1),
                              Hexagon(-2,0)),
                          Set(Hexagon(1,2),
                              Hexagon(1,-1),
                              Hexagon(-2,0)))
                              
  val scrambl3aj = ScramblPattern(Set(Hexagon(-1,2),
                              Hexagon(-1,-1),
                              Hexagon(2,0)),
                          Set(Hexagon(-1,2),
                              Hexagon(-1,-1),
                              Hexagon(2,0)))
                              
  val scrambl3bi = ScramblPattern(Set(Hexagon(1,1),
                              Hexagon(-1,1),
                              Hexagon(0,-1)),
                          Set(Hexagon(1,1),
                              Hexagon(-1,1),
                              Hexagon(0,-1)))
   
  val scrambl3bj = ScramblPattern(Set(Hexagon(0,1),
                              Hexagon(-1,0),
                              Hexagon(1,0)),
                          Set(Hexagon(0,1),
                              Hexagon(-1,0),
                              Hexagon(1,0)))
                              
  val scrambl4ai = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(0,1),
                              Hexagon(-1,0),
                              Hexagon(1,0)),
                          Set(Hexagon(0,1),
                              Hexagon(-1,0),
                              Hexagon(1,0)))
                              
  val scrambl4aj = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(1,1),
                              Hexagon(-1,1),
                              Hexagon(0,-1)),
                          Set(Hexagon(1,1),
                              Hexagon(-1,1),
                              Hexagon(0,-1)))
                              
  val scrambl5ai = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(1,1),
                              Hexagon(1,0),
                              Hexagon(-1,1),
                              Hexagon(-1,0)),
                          Set(Hexagon(-2,0),
                              Hexagon(2,0)))
                              
  val scrambl5aj = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(0,1),
                              Hexagon(1,1),
                              Hexagon(-1,0),
                              Hexagon(0,-1)),
                          Set(Hexagon(1,2),
                              Hexagon(-1,1)))
                              
  val scrambl5ak = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(0,1),
                              Hexagon(-1,1),
                              Hexagon(1,0),
                              Hexagon(0,-1)),
                          Set(Hexagon(-1,2),
                              Hexagon(1,-1)))
                              
  val scrambl6a = ScramblPattern(Set(Hexagon(0,2),
                              Hexagon(-2,1),
                              Hexagon(-2,-1),
                              Hexagon(0,-2),
                              Hexagon(2,-1),
                              Hexagon(2,1)),
                          Set(Hexagon(0,0)))
                              
  val scrambl7a = ScramblPattern(Set(Hexagon(0,0),
                              Hexagon(-1,2),
                              Hexagon(1,2),
                              Hexagon(2,0),
                              Hexagon(1,-1),
                              Hexagon(-1,-1),
                              Hexagon(-2,0)),
                          Set(Hexagon(0,0)))
                          
  val scrambls = Seq(
      scrambl1,
      scrambl2ai, scrambl2aj, scrambl2ak,
      scrambl3ai, scrambl3aj, scrambl3bi, scrambl3bj,
      scrambl4ai, scrambl4aj,
      scrambl5ai, scrambl5aj, scrambl5ak,
      scrambl6a,
      scrambl7a)
}