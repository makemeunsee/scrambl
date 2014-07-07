package hexatext.mvc.controller

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.{ClickListener, Align}
import widgets.{RollingLabel, DefaultMenu}

object RoamingMenu {
  val nextLevelAction = "Next level"
  val newGameAction = "New game"
}

import RoamingMenu._

class RoamingMenu(ctrl: RoamingController)(implicit skin: Skin) extends DefaultMenu {
  
  private val model = ctrl.model
  
  // small text console to display status info
  val console = new RollingLabel("", skin, 3)
  console.setAlignment(Align.center)
  console.setStyle(new LabelStyle(console.getStyle) { style =>
    style.background = skin.getDrawable("menu_button")
  })

  protected def consoleSize = (400, 70)

  // button to restart game / go to next level
  val nlButton = new TextButton(nextLevelAction, skin)
  nlButton.setVisible(false)
  nlButton.addListener(new ClickListener(0) {

    override def clicked(e: InputEvent, x: Float, y: Float) {
      if (nlButton.getText.toString == nextLevelAction) {
        ctrl.nextLevel()
        updateScore()
      } else if (nlButton.getText.toString == newGameAction) {
        ctrl.resetGame()
        updateScore()
      }
      ctrl.initLevel()
      helpLabel.setText(helpText)
      nlButton.setVisible(false)
      qButton.setVisible(false)
//      krButton.setVisible(false)
    }

  })

  protected def nlButtonSize = (120, 40)
  
//  // button to kill/revive branches after the game is won
//  val krButton = new TextButton("Kill / Revive", skin)
//  krButton.setVisible(false)
//  krButton.addListener(new ClickListener(0) {
//
//    override def clicked(e: InputEvent, x: Float, y: Float) {
//      ctrl.reviveOrKill()
//    }
//
//  })
//
//  protected def krButtonSize = (120, 40)

  // quit button
  val qButton = new TextButton("Quit", skin)
  qButton.setVisible(false)
  qButton.addListener(new ClickListener(0) {
    override def clicked(e: InputEvent, x: Float, y: Float) {
      Gdx.app.exit()
    }
  })

  protected def qButtonSize = (120, 40)

  // help dialog
  protected val helpLabel = new Label(helpText, skin)
  helpLabel.setVisible(false)
  helpLabel.setAlignment(Align.left)
  helpLabel.setStyle(new LabelStyle(helpLabel.getStyle) { style =>
    style.background = skin.getDrawable("menu_button")
  })

  // help button
  protected val helpButton = new Button(skin, "help")
  helpButton.addListener(new ClickListener(0) {
    override def clicked(e: InputEvent, x: Float, y: Float) {
      toggleHelp()
    }
  })

  def toggleHelp() { helpLabel.setVisible(!helpLabel.isVisible) }
  
  def updateScore() { scoreLabel.setText(s"Score: ${ctrl.score} - Level: ${model.level}") }

  protected def helpTextLines = Seq(
    "How to play:",
    "Rebuild the broken flake one step at a time.",
    "Click/touch a glowing tile to add it to the flake.",
    "Make sure it's part of the flake first!",
    "Drag to move around, scroll/pinch to zoom in and out.",
    "",
    s"Playing on seed: ${model.currentSeed}")
  private def helpText = helpTextLines.mkString("\n")
  protected def helpTextWidth = 600
  protected def helpTextLineHeight = 20

  // score label
  protected val scoreLabel = new Label(s"Score: 0 - Level: ${model.level}", skin) {
    override def getPrefWidth = {
      super.getPrefWidth + 30
    }
    override def getPrefHeight = {
      super.getPrefHeight + 8
    }
  }
  scoreLabel.setAlignment(Align.center)

  addActor(new Table { consolePane =>
    consolePane.setFillParent(true)
    consolePane.add(console).width(consoleSize._1).height(consoleSize._2)
    consolePane.top()
  })

  addActor(new Table { middlePane =>
    middlePane.setFillParent(true)
    middlePane.add(nlButton).width(nlButtonSize._1).height(nlButtonSize._2)
//    middlePane.row()
//    middlePane.add(krButton).width(krButtonSize._1).height(krButtonSize._2)
    middlePane.row()
    middlePane.add(qButton).width(qButtonSize._1).height(qButtonSize._2)
  })

  addActor(new Table { statusPane =>
    statusPane.setFillParent(true)
    statusPane.add(scoreLabel).padRight(15)
    statusPane.right().top()
  })

  addActor(new Table { helpPane =>
    helpPane.setFillParent(true)
    helpPane.add(helpButton).padLeft(5).padTop(5)
    helpPane.left().top()
  })

  addActor(new Table { helpTable =>
    helpTable.setFillParent(true)
    helpTable.add(helpLabel).width(helpTextWidth).height(helpTextLines.size * helpTextLineHeight)
  })

  toggle()
  nlButton.setText("Begin")
  nlButton.setVisible(true)
}
