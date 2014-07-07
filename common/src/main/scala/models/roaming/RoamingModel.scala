package models.roaming

import world2d.Hexagon
import scala.util.Random
import models.DefaultModel
import models.FlakeGenerator
import world2d.LivingHexagon
import DefaultModel.now

import RoamingModel._

sealed trait RoamingGameState
case object Off extends RoamingGameState
case object Intact extends RoamingGameState
case object Dying extends RoamingGameState
case object Dead extends RoamingGameState
case object Playing extends RoamingGameState
case object Won extends RoamingGameState

object RoamingModel {
  // some delay in ms used to build parts of the flake in game
  val branchBirthTimeStep = 500
  
  // origin hexa, not shown and not part of the game, so essential
  val h0 = new LivingHexagon(0,0)

  // ref branch flickering period
  val flickerPeriod = 2000 //ms
}

// a game model where missing branches of a flake have to be rebuilt
case class RoamingModel(seed0: Long = Random.nextLong(),
                        sizeX: Int = 60,
                        sizeY: Int = 60) extends DefaultModel with FlakeGenerator {

  private var centerHexa = new LivingHexagon(0, 0)

  override def onGrid = Set(centerHexa)
  
  override def worldLimits = (new LivingHexagon(-sizeX/2, -sizeY/2).center, new LivingHexagon(sizeX/2, sizeY/2).center)
  
  def compact = false

  override protected def stopper = { case (id: Int, hex: Hexagon) =>
    hex.x < sizeX/2/flakeSpacing &&
    hex.x > -sizeX/2/flakeSpacing &&
    hex.y < sizeY/2/flakeSpacing &&
    hex.y > -sizeY/2/flakeSpacing
  }
  
  // model state contains:
  
  // the current game state
  var state: RoamingGameState = Off
  
  // the flake branches, handling their own inner state
  private var v_branches = Seq.empty[BranchModel]

  // the current game level
  private var v_level = 1
  
  def level = v_level  
  
  // if the game is lost
  def lost = otherBranches.forall(_.dead)
  // if the game is won
  def won = otherBranches.exists(b => !b.dead && b.levelComplete)
  // how many branches (ref included) are still alive
  def liveBranches = v_branches.count(!_.dead)
  
  // reset the game
  def reset() {
    centerHexa = new LivingHexagon(centerHexa.x, centerHexa.y, now, Float.MaxValue)
    next() // use a new flake
    v_level = 1
    state = Off
    v_branches = flake.map(b => new BranchModel(b.tail)) // ignore first hexa as it's Hex(0,0) (common to all branches, not playable)
    v_branches.foreach(_.setLevel(v_level))
  }
  
  // check if the next level is playable
  def hasNextLevel = {
    refBranch.setLevel(v_level+1)
    val result = refBranch.playableHexas.nonEmpty
    // restore level
    refBranch.setLevel(v_level+1)
    result
  }
  
  def nextLevel() {
    state = Off
    v_level += 1
    v_branches.foreach(_.setLevel(v_level))
  }

  // kill all live branches
  def killLiveBranches() = {
    centerHexa = new LivingHexagon(centerHexa.x, centerHexa.y, centerHexa.birthtime, now + LivingHexagon.agonyDuration)
    v_branches.filter(!_.dead).flatMap(_.kill())
  }

  // the reference branch which is used as model when rebuilding the flake
  def refBranch = v_branches(0)
  // the branches to rebuild
  private def otherBranches = v_branches.tail
  // the branches to rebuild, not dead
  private def playableBranches = v_branches.tail.filter(!_.dead)
  
  // take a step, play the game
  def step(h: LivingHexagon): (Boolean, Seq[LivingHexagon]) = {
    playableBranches
    .find(_.nextSteps.contains(h)) match {
      
      case Some(branch) =>
        val (success, changed) = branch.step(h)
        if (success) {
          val symSteps = playableBranches
            .filter(b => !b.dead && b != branch)
            .flatMap(_.autoNextStep())
          (true, changed ++ symSteps)
        } else
          (false, changed)
        
      case _            =>
        (false, Seq.empty)
    }
  }

  def harmonizeBirthTimes() = { v_branches.filter(!_.dead).flatMap(_.harmonizeBirthTimes()) }

  // how long the 'intact' state animation takes to complete
  def sproutingDuration = (refBranch.branchDuration + branchBirthTimeStep) * liveBranches

  // sprout all live branches (complete them)
  def sprouts() = {
    val time = now
    val branchLimit = time + sproutingDuration
    val branchStep = refBranch.branchDuration + branchBirthTimeStep
    v_branches
    .filter(!_.dead)
    .zipWithIndex
    .flatMap { case (branch, branchId) =>
      val branchInit = time + branchId * branchStep
      // branches sprout smoothly, one hex a precise time after the last
      val birth: Option[LivingHexagon] => Float = {
        case Some(h) if h.birthtime >= branchInit => h.birthtime + BranchModel.hexBirthTimeStep
        case _                                    => branchInit
      }
      // all but the ref branch die after sprouting, within 2 secs
      val death: Option[LivingHexagon] => Float = _ =>
        if (branchId == 0) Float.MaxValue
        else branchLimit + 500 + Random.nextInt(1500)
      branch.autoComplete(birth, death)
    }
  }

  // withers all playable branches (empty the playable portion)
  def withers() = {
    playableBranches
    .flatMap(_.wither())
  }
  
//  // revive any dead branch, if none kill all branches
//  def reviveOrKillAll(t0: Float = now) = {
//    if (v_branches.exists(_.dead)) v_branches.filter(_.dead).flatMap(_.revive(t0))
//    else v_branches.flatMap(_.kill())
//  }

  def live = v_branches
      .filter(!_.dead)
      .flatMap(_.builtHexas)

  def dead = {
    val deads = v_branches
      .filter(_.dead)
      .flatMap(_.builtHexas)
    if (state == Dead) centerHexa +: deads
    else deads
  }

  // hexas which can be clicked to play the game
  def nextSteps: Seq[LivingHexagon] = state match {
    case Playing =>
      playableBranches
        .flatMap(_.nextSteps)
        .filter(canBeNextStep)

    case _       => Seq.empty
  }

  private def canBeNextStep(h: Hexagon): Boolean = {
    h != h0 && !(live ++ dead).contains(h)
  }
}
