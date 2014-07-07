package hexatext.mvc.controller

import com.badlogic.gdx.InputProcessor
import rendering.shaders.GdxShadersPack
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import rendering.screen.RoamingScreen
import com.badlogic.gdx.InputAdapter
import threading._
import models.roaming.{RoamingModel, Intact, Dying, Playing, Dead, Won}
import world2d.LivingHexagon

class RoamingController(val model: RoamingModel, val screen: RoamingScreen) extends InputAdapter with StdController {
  
  override val inputProcessor: InputProcessor = new DefaultScreenInputProcessor(screen, this)

  // failures count during a single level
  private var levelFailures = 0
  
  // score accumulator
  private var v_score = 0
  def score = v_score
  
  def init() {
    // init screen
    screen.setBackgroundShader(GdxShadersPack.LavaBasaltGradient.backgroundShader)
    resetGame()
  }
  
  private[controller] def resetGame() {
    model.reset()
    // reset needs to occur first so screen loads the correct values
    screen.updateForegroundBlocks()
    screen.setBlockShader(screen.intactShader, recreateMesh = true)
    perf.printResults()
  }
  
  private[controller] def nextLevel() { model.nextLevel() }
  
//  // revive any dead branch, if none kill all branches
//  private[controller] def reviveOrKill() {
//    val t0 = DefaultModel.now
//    // dead hexas are rendered depending on their birth time mod 6
//    // ensure the new birth time gives the same behavior
//    val birthBase = model.refBranch.at(0).map(_.birthtime).getOrElse(0f)
//    val shift = 6 + birthBase % 6 - t0 % 6
//    val undead = model.reviveOrKillAll(t0 + shift)
//    notifyDead(undead)
//  }
  
  private[controller] def initLevel() {
    levelFailures = 0
    
    // starts by showing the intact flake at this level
    val live = model.sprouts()
    model.state = Intact
    notifyLive(live)
    //notifyDead()

    introCancel = new CancelBox

    val f = Future {

      // wait for the flake to grow fully
      skippableWait(model.sproutingDuration)
      
      // activate 'dying mode'
      onGdxThread {
        model.state = Dying
      }

      // wait for end of branch death
      skippableWait(2000)
    }
    
    // start playing
    f onComplete { case _ =>
      onGdxThread {
        val withered = model.withers()
        model.state = Playing
        // notify withered hex as long dead so they disappear immediately
        // (required when animation is skipped)
        notifyLive(withered.map(lh => new LivingHexagon(lh.x, lh.y, 0, 0)))
      }
    }
  }

  protected implicit var introCancel = new CancelBox

  private def gameover() {
    v_score = 0
    val dead = model.killLiveBranches()
    model.state = Dead
    notifyLive(dead)
    notifyDead()
    menu.console.addLine("GAME OVER!")

    delayedGdxAction(2000, {
      menu.nlButton.setText(RoamingMenu.newGameAction)
      menu.nlButton.setVisible(true)
      menu.qButton.setVisible(true)
    })
  }

  private def scoring = {
    val liveBranches = model.liveBranches
    val completionBonus = model.level * liveBranches
    menu.console.addLine(s"Solved level ${model.level} with $liveBranches live branches: +$completionBonus")

    val perfection = if (liveBranches == 6) {
      menu.console.addLine(s"Perfect bonus: score X 2!")
      2
    } else 1

   val malus = levelFailures * 6
    if (levelFailures > 0)
      menu.console.addLine(s"$levelFailures failed branches: -$malus")

    completionBonus * perfection - malus
  }
  
  private def endLevel() {

    v_score += scoring
    menu.updateScore()

    levelFailures = 0
    
    model.state = Won
    val changed = model.harmonizeBirthTimes()
    notifyLive(changed)

    introCancel = new CancelBox

    skippableDelayedGdxAction(2000, {
      if (model.hasNextLevel) {
        menu.nlButton.setText("Next level")
        menu.nlButton.setVisible(true)
      } else {
        menu.console.addLine(s"Last level complete! Final score: *** $v_score ***")
        menu.nlButton.setText(RoamingMenu.newGameAction)
        menu.nlButton.setVisible(true)
        menu.qButton.setVisible(true)
//        menu.krButton.setVisible(true)
      }
    })
  }

  private def notifyLive(hexas: Seq[LivingHexagon]) {
    screen.updateBlocks(hexas.toSet)
  }

  private def notifyDead() {
    screen.updateDead()
  }

  override def continuousActions(): Unit = {}

  def clickAction(hexa: LivingHexagon) {
    (model.state, model.nextSteps) match {
      // skip animation
      case (Won, _ ) | (Intact, _) | (Dying, _) =>
        introCancel.cancelled = true

      // actual gameplay
      case (Playing, seq) if seq.contains(hexa) =>
        val (success, changed) = model.step(hexa)

        if (success) {
          notifyLive(changed)
          if (model.won) {
            // the reference branch is never actually built in game
            // we fill it here so it shows properly in the next levels
            model.refBranch.autoComplete()
            menu.console.addLine("Level complete!")
            // wait for last click action to complete on screen
            delayedGdxAction(LivingHexagon.agonyDuration.toLong, {
              endLevel()
            })
          }
        } else {
          // notify lost hexas as long dead to avoid default death animation
          notifyLive(changed.reverse.tail.map(lh => new LivingHexagon(lh.x, lh.y, 0, 0)))
          notifyDead()
          if (model.lost) gameover()
          else {
            levelFailures += 1
            menu.console.addLine("Stay on the path or regret it!")
          }
        }
        
      case _ => () // ignore click
    }
  }

  import widgets.skin
  val menu = new RoamingMenu(this)
}
