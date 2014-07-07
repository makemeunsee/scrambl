package models.roaming

import world2d.LivingHexagon
import models.DefaultModel.now

object BranchModel {
  // interval used when 'sprouting' or 'killing' successive hexagons in a branch
  val hexBirthTimeStep = 50
}

import BranchModel._

// part of a roaming model, handling the failed/rebuilt states of a branch of a flake
// underlying is the full branch of which the BranchModel gives different views 
class BranchModel(underlying: Seq[LivingHexagon]) {

  // is this branch dead or playable?
  private var v_dead = false
  def dead = v_dead
  
  // kill this branch, from tip to root
  def kill() = {
    v_dead = true
    val death = now + LivingHexagon.agonyDuration
    v_built = v_built
      .reverse
      .zipWithIndex
      .map { case (h, id) =>
        new LivingHexagon(h.x, h.y, h.birthtime, death + id * hexBirthTimeStep)
      }
      .reverse
    v_built
  }
  
  // revive this branch, from root to tip
  def revive(time: Float = now) = {
    v_dead = false
    val birth = time
    v_built = v_built
      .zipWithIndex
      .map { case (h, id) =>
        new LivingHexagon(h.x, h.y, birth + id * hexBirthTimeStep, Float.MaxValue)
      }
    v_built
  }
  
  // index in the underlying branch of the next valid step
  private var v_currId = 0

  // current position in the branch
  def currentId = v_currId

  def at(id: Int) = v_built.lift(id)
  
  // rebuilt, properly played steps
  private var v_built = Seq.empty[LivingHexagon]
  def builtHexas = v_built
  
  // take a step, maybe succeed or fail and kill the whole branch
  def step(h: LivingHexagon, birth: Float = now, death: Float = Float.MaxValue): (Boolean, Seq[LivingHexagon]) = {
    v_built = v_built :+ new LivingHexagon(h.x, h.y, birth, death)
    if (underlying(v_currId) == h) {
      v_currId += 1
      (true, Seq(v_built.last)) // success
    } else {
      (false, kill()) // failure
    }
  }

  // make it so consecutive hexas have a birth delta of birthDelta.
  // the first hexa in the branch gets birth0 as birthtime.
  def harmonizeBirthTimes(birth0: Float = 0, birthDelta: Float = hexBirthTimeStep) = {
    v_built = v_built.foldLeft(Seq.empty[LivingHexagon]) { case (acc, h) =>
      val newBirthTime = acc.lastOption.fold(birth0)(_.birthtime + birthDelta)
      val newHex = new LivingHexagon(h.x, h.y, newBirthTime, h.deathtime)
      acc :+ newHex
    }
    v_built
  }
  
  // functions to maintain proper state
  // take the next adequate step
  def autoNextStep(birth: Float = now, death: Float = Float.MaxValue) = step(underlying(v_currId), birth, death)._2

  // take all the next adequate steps to complete the current level
  // birth and death function use the previous step as parameter
  // and are used to define the birth and death times of the next step
  def autoComplete(birth: Option[LivingHexagon] => Float = h => h.fold(now + hexBirthTimeStep)(_.birthtime),
                   death: Option[LivingHexagon] => Float = _ => Float.MaxValue) = {
    val start = v_currId
    (start until branchLength)
    .flatMap { i =>
      step(underlying(v_currId), birth(at(v_currId-1)), death(at(v_currId-1)))._2
    }
  }

  // remove all hexas of the currently playable portion
  def wither() = {
    val goalSize = branchLength - playedLength
    val diff = v_built.size - goalSize
    if (diff > 0) {
      val res = v_built.takeRight(diff)
      v_built = v_built.dropRight(diff)
      v_currId -= diff
      res
    } else {
      Seq.empty[LivingHexagon]
    }
  }
  
  // current level
  // must be >= 1 for valid behavior
  private var v_level = 0
  def setLevel(level: Int) { v_level = level }

  private def branchLengthAtLevel(level: Int) = (level * (level+1))/2 * 6
  
  // full length of the branch at this level
  def branchLength = math.min(branchLengthAtLevel(v_level), underlying.size)
  
  // playable length of the branch at this level
  def playedLength = branchLength - branchLengthAtLevel(v_level-1)
  
  // playable portion of the branch: the last 'playedLength' hexas of the branch
  def playableHexas = underlying.drop(branchLength - playedLength).take(playedLength)
  
  // if the branch at this level is fully rebuilt
  def levelComplete = builtHexas.nonEmpty && builtHexas.last == playableHexas.last
  
  // how long it takes for a branch to be ready (animation wise)
  def branchDuration = playedLength * BranchModel.hexBirthTimeStep
  
  // next steps possible from the tip of this branch
  def nextSteps =
    if (dead || v_currId == branchLength) Seq.empty
    else if (v_built.isEmpty) Seq(underlying.head)
    else v_built
      .last
      .neighbours
      .map(new LivingHexagon(_, 0, Float.MaxValue))
      .filter(h => !v_built.takeRight(7).contains(h))
}

