package widgets

import com.badlogic.gdx.scenes.scene2d.ui.Table
import hexatext.mvc.controller.{FlakeGameController, ScreenController}
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.Touchable
import hexatext.mvc.model.FlakeModel
import models.FlakeGenerator

class ExplorerMenu(controller: FlakeGameController with ScreenController)(implicit skin: Skin) extends DefaultMenu {
  
  val colorWidget1 = new ColorWidget
  val colorWidget2 = new ColorWidget
  val colorWidget3 = new ColorWidget

  val flakeSizeSlider = new SilentSlider(0, FlakeGenerator.sizeMax+1, 1, false)

  createTable()

  private def createTable() {

    colorWidget1.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        controller.updateBlockColor(colorWidget1.getViewColor)
      }
    })

    colorWidget2.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        controller.updateBackgroundColor0(colorWidget2.getViewColor)
      }
    })

    colorWidget3.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        controller.updateBackgroundColor1(colorWidget3.getViewColor)
      }
    })

    flakeSizeSlider.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        controller.flakeSize(flakeSizeSlider.getValue.toInt)
      }
    })
    
    val table = new Table
    table.setTouchable(Touchable.enabled) //TODO
    table.setFillParent(true)
    addActor(table)
    
    val colorsTable = new Table {
      add(colorWidget1)
      add(colorWidget2)
      add(colorWidget3)
    }
    table.add(colorsTable)
    table.row()
    
    val alphaRow = new Table {
      val alphaSpeedSlider = new SilentSlider(0.5f, 10f, 0.5f, false) {
        setValue(1f)
        addListener(new ChangeListener {
          override def changed(event: ChangeEvent, actor: Actor): Unit = {
            controller.setAlphaSpeed(getValue)
          }
        })
      }
      
      val alphaSpeedLabel = new Label("blending rate", skin)

      val alphaRate = new Table {
        setBackground(skin.getDrawable("menu_button"))
        add(alphaSpeedLabel)
        row()
        add(alphaSpeedSlider)
      }
      add(alphaRate)
      
    }
    table.add(alphaRow)
    table.row()
    
    val flakeControl = new Table {
      setBackground(skin.getDrawable("menu_button"))
      add(new Label("flake size", skin))
      row()
      add(flakeSizeSlider)
    }
    
    table.add(flakeControl)
    table.row()
    
    val firstButtonRow = new Table {
      add(button("full_random", {
        controller.randomizeBackgroundColors()
        controller.randomizeBlockColor()
      }))
      
      add(button("front_random", {
        controller.randomizeBlockColor()
      }))
      
      add(button("back_random", {
        controller.randomizeBackgroundColors()
      }))
      
      add(button("noise_random", {
        controller.reseedShaders()
      }))
      
      add(button("flake", {
        controller.nextLevel()
      }))
    }
    table.add(firstButtonRow)
    table.row()
    
    val secondButtonRow = new Table {
      add(button("left", {
        controller.previousPredefShader()
      }))
      
      add(button("right", {
        controller.nextPredefShader()
      }))
    }
    table.add(secondButtonRow)
    table.row()

    table.top()
    
    addActor(table)
  }
}