package hexatext.mvc.model

import scala.util.Random
import world2d.{Point, Hexagon, LivingHexagon}
import models.scrambl.Scrambl
import com.badlogic.gdx.graphics.Mesh
import models.DefaultModel
import models.FlakeGenerator
import DefaultModel.now

// TODO not thread safe => move to actor model?
class FlakeModel(val seed0: Long = Random.nextLong()) extends DefaultModel with FlakeGenerator {

  def compact = true

  protected var allHexas = Set.empty[LivingHexagon]

  def onGrid: Set[LivingHexagon] = allHexas

  def aliveAt(time: Long) = onGrid.filter(_.alive(time))
  def deadAt(time: Long) = onGrid.filter(!_.alive(time))

  override def at(p: Point): LivingHexagon = {
    val hex = super.at(p)
    allHexas.find(_ == hex) match {
      case Some(fh) => fh
      case None     => new LivingHexagon(hex.x, hex.y)
    }
  }

  // hexas: hexagons to toggle in the model
  // within: time within which to toggle
  def toggle(hexas: Set[_ <: Hexagon], within: Float = 0): Set[LivingHexagon] = {
    val time = now
    hexas.map { hexa =>
      val ownTime = time + Random.nextFloat() * within
      val (x,y) = (hexa.x, hexa.y)
      val hOpt = allHexas.find(_ == hexa)
      hOpt match {
        case Some(h) =>
          allHexas -= h
          if ( h.deathtime > time + LivingHexagon.agonyDuration) {
            // case hexa 'alive': kill it
            val newHexa = new LivingHexagon(x, y, h.birthtime, ownTime + LivingHexagon.agonyDuration)
            allHexas += newHexa
            newHexa
          } else if ( h.deathtime > time ) {
            // case hexa 'dying': cancel death
            val newHexa = new LivingHexagon(x, y, 2*ownTime - h.deathtime, Float.MaxValue)
            allHexas += newHexa
            newHexa
          } else {
            // case hexa 'dead': resurrect
            val newHexa = new LivingHexagon(x, y, ownTime, Float.MaxValue)
            allHexas += newHexa
            newHexa
          }
        case None =>
          // no previous hexa -> create live one
          val newHexa = new LivingHexagon(x, y, ownTime, Float.MaxValue)
          allHexas += newHexa
          newHexa
      }
    }
  }

  private var level = -1
  private val scrambl0 = Scrambl(Hexagon(0,0), Scrambl.scrambls(0))
  
  // initial values discarded in init
  protected var flakeView = Set.empty[LivingHexagon]
    
  private def init(startingHex: LivingHexagon = new LivingHexagon(0,0)) {
    // clear level
    allHexas = Set.empty
    next()
    flakeView = flake.transpose.flatten.take(flakeSize * 6).toSet + startingHex
    // activate flake
    println(s"${allHexas.size}, ${Mesh.getManagedStatus}")
  }
  
  def flakeSize = 25 + 10 * level
  
  def resizeFlake(size: Int): Set[LivingHexagon] = {
    Set.empty // do nothing, size is controlled by level
  }

  // hexagons of the model, actual state of the model
  def target = flakeView
  
  def scrambls: Seq[Scrambl] = {
    val targets = scramblTargets
    val rnd = new Random(currentSeed)
    (1 to level).foldLeft(Seq(scrambl0)) { case (acc, _) =>
      val id = rnd.nextInt(targets.size)
      Scrambl(targets(id), Scrambl.scrambls(0)) +: acc
    }
  }
  
  private def scramblTargets = target.flatMap(_.neighbours).flatMap(_.neighbours).toSeq

  def nextLevel() {
    level += 1
    init()
  }

  def success = {
    val afterAgony = now + LivingHexagon.agonyDuration
    onGrid.filter(_.alive(afterAgony)) == target
  }
  
  def newGame() = {
    level = 0
    init()
  }
}