package hexatext.mvc.controller

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import com.badlogic.gdx.{InputProcessor, Gdx, InputAdapter}
import world2d.Hexagon
import hexatext.mvc.model.FlakeModel
import world2d.LivingHexagon
import rendering.screen.{ForegroundScreen, DefaultScreen, ScramblScreen}
import scala.util.Random
import rendering.shaders._
import widgets.{DefaultMenu, Menu}
import controllers.GameController
import rendering.shaders.{NoFX, Border, Shrinking, HighlightMode, Fading, Squeezing, Shaking, Pulsating}

trait FlakeGameController extends GameController {

  override def model: FlakeModel
  
  def flakeSize(i: Int) = {
    if (!model.locked) {
      onChange(model.resizeFlake(i))
    }
  }

  def nextLevel() {
    model.lock()
    val nextLevelAction = nextLevelFuture
    nextLevelAction onSuccess { case _ => model.unlock() }
    nextLevelAction onFailure { case t => t.printStackTrace() ; model.unlock() }
  }
  
  private def asyncModelUpdate(hexas: Set[_ <: Hexagon], sleepTime: Long) {
    val changed = model.toggle(hexas)
    Gdx.app.postRunnable(new Runnable {
      override def run() {
        onChange(changed)
      }
    })
    Thread.sleep(sleepTime)
  }

  private val delay = LivingHexagon.agonyDuration.toLong

  private def nextLevelFuture = Future {
    // transition to next level
    
    perf.printResults()

    val tNext = System.currentTimeMillis
    
    // leave completed level on screen for a bit
    val onlyAlive = model.aliveAt(tNext)

    if ( onlyAlive.nonEmpty ) {
      // kill the old hexas within 1 sec
      val changed = model.toggle(onlyAlive, 1000)
      Gdx.app.postRunnable(new Runnable {
        override def run() {
          onChange(changed)
        }
      })
      // wait for all hexas to be live
      Thread.sleep(1000)
    }

    // load the next level
    model.nextLevel()

    // toggle the new flake witin 1 sec
    Thread.sleep(delay)
    val changed = model.toggle(model.target, 1000)
    Gdx.app.postRunnable(new Runnable {
      override def run() {
        onChange(changed)
      }
    })
    // wait for all hexas to be live
    Thread.sleep(1000)

    // scramble it
    afterNextLevel()
  }

  protected def afterNextLevel() {
    // apply scrambls every half second
    for (s <- model.scrambls ) {
      asyncModelUpdate(s.exploded, delay / 2)
    }
  }
}

trait ScreenController {
  def screen: ScramblScreen
  
  protected def applyBlockShader(shader: GenericShaderModule, recreateMesh: Boolean = true) {
    screen.setBlockShader(shader, recreateMesh)
  }

  protected def applyBackgroundShader(shader: GenericShaderModule, recreateMesh: Boolean = true) {
    screen.setBackgroundShader(shader, recreateMesh)
  }
  
  import rendering.shaders.GdxShadersPack
  
  private var shaderEnumId = GdxShadersPack.values.indexOf(GdxShadersPack.LavaBasaltGradient)
  def applyShader(shader: GdxShadersPack.ShaderPair) {
    shaderEnumId = GdxShadersPack.values.indexOf(shader)
    applyPredefShader()
  }
  def applyPredefShader() {
    applyBlockShader(GdxShadersPack.values(shaderEnumId).blockShader)
    applyBackgroundShader(GdxShadersPack.values(shaderEnumId).backgroundShader)
  }
  def nextPredefShader() {
    shaderEnumId = (shaderEnumId + 1) % GdxShadersPack.values.size
    applyPredefShader()
  }
  def previousPredefShader() {
    shaderEnumId = (shaderEnumId - 1 + GdxShadersPack.values.size) % GdxShadersPack.values.size
    applyPredefShader()
  }

  def reseedShaders() {
    val blockColor = screen.blockGenericShader.color0.reseed
    applyBlockShader(screen.blockGenericShader.copy(blockColor, blockColor))
    applyBackgroundShader(screen.backgroundGenericShader.copy(screen.backgroundGenericShader.color0.reseed, screen.backgroundGenericShader.color1.reseed))
  }

  def updateBlockColor(color: GdxColor) {
    val bs = screen.blockGenericShader
    val newColor = bs.color0.copy(baseColor = color)
    applyBlockShader(bs.copy(color0 = newColor, color1 = newColor))
  }

  def updateBackgroundColor0(color: GdxColor) {
    val bbs = screen.backgroundGenericShader
    applyBackgroundShader(bbs.copy(color0 = bbs.color0.copy(baseColor = color)))
  }

  def updateBackgroundColor1(color: GdxColor) {
    val bbs = screen.backgroundGenericShader
    applyBackgroundShader(bbs.copy(color1 = bbs.color1.copy(baseColor = color)))
  }

  def randomizeBlockColor() {
    val bs = screen.blockGenericShader
    
    val blockCubic = Random.nextBoolean()
    val blockBorder = if (Random.nextBoolean()) NoFX else Border(randomColor(randomAlpha = true), 0.8f + 0.8f * Random.nextInt(4))
    val sproutingMode = if (Random.nextBoolean()) Shrinking else Fading
    val blockGColor = bs.color0.copy(baseColor = randomColor(randomAlpha = false), noiseScalingX = randomNoiseScale, noiseScalingY = randomNoiseScale, noiseCoeffs = randomNoiseCoeffs)
    val newBlockShaderTool = bs.copy(color0 = blockGColor,
                                     color1 = blockGColor,
                                     border = blockBorder,
                                     sprouting = sproutingMode,
                                     cubic = blockCubic)

    applyBlockShader(newBlockShaderTool)
  }

  def randomizeBackgroundColors() {
    val bgs = screen.backgroundGenericShader

    val highlightMode: HighlightMode = if (Random.nextBoolean()) randomHighlight() else NoFX
    val backgroundCubic = Random.nextBoolean()
    val rndCol = randomColor(randomAlpha = false)
    val darkerRndCol = rndCol.mul(0.5f, 0.5f, 0.5f, 1f)
    val backgroundBorder = if (Random.nextBoolean()) NoFX else Border(randomColor(randomAlpha = true), 0.8f + 0.8f * Random.nextInt(4))
    val backgroundGColor0 = bgs.color0.copy(baseColor = rndCol, noiseScalingX = randomNoiseScale, noiseScalingY = randomNoiseScale, noiseCoeffs = randomNoiseCoeffs)
    val backgroundGColor1 = bgs.color1.copy(baseColor = darkerRndCol, noiseScalingX = randomNoiseScale, noiseScalingY = randomNoiseScale, noiseCoeffs = randomNoiseCoeffs)
    val newBackgroundShaderTool = bgs.copy(color0 = backgroundGColor0,
                                           color1 = backgroundGColor1,
                                           border = backgroundBorder,
                                           highlighting = highlightMode,
                                           cubic = backgroundCubic)

    applyBackgroundShader(newBackgroundShaderTool)
  }
  
  private def randomColor(randomAlpha: Boolean): rendering.Color =
    new GdxColor( if (randomAlpha) Random.nextInt() else Random.nextInt() | 0x000000ff )
  
  private def randomNoiseScale = 0.01f * math.pow(2, Random.nextInt(8)).toFloat
  private def randomNoiseCoeff = ((1+Random.nextInt(10)) * math.pow(10, Random.nextInt(3)-1)).toFloat
  private def randomNoiseCoeffs = (randomNoiseCoeff,randomNoiseCoeff,randomNoiseCoeff)
  
  private def randomHighlight(): HighlightMode = Random.nextInt(3) match {
    case 0 => randomPulsation()
    case 1 => Shaking()
    case 2 => Squeezing()
  }
  
  private def randomPulsation(): Pulsating = {
    Pulsating(rate = (Random.nextInt(20)+1) /10f,
              amplitude = (Random.nextInt(21) + 1) / 10f,
              shift = (Random.nextInt(5) - 3) / 2f)
  }

  def setAlphaSpeed(rate: Float)  {
    applyBackgroundShader(screen.backgroundGenericShader.copy(blendingRate = rate), recreateMesh = false)
  }
}

trait ScreenInputProcessor extends InputAdapter {
  
  def screen: DefaultScreen[LivingHexagon] with ForegroundScreen
  
  private var lastCoords = (0,0)
  def lastMousePosition = lastCoords
  
  override def scrolled(amount: Int): Boolean = {
    if (amount > 0) screen.zoomOut() else screen.zoomIn()
    true
  }

  override def mouseMoved(x: Int, y: Int) = {
    lastCoords = (x, y)
    true
  }

  override def touchDragged(x: Int, y: Int, pointer: Int) = {
    screen.moveCam(lastCoords._1 - x, y - lastCoords._2)
    mouseMoved(x, y)
    true
  }  
  
  private var clicks = Map.empty[Int, (Int, Int)]
  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = {
    clicks += ((button, (screenX, screenY)))
    true
  }
  
  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = {
    if ( button == 0 )
      clicks.get(button) match {
        case Some((oldX, oldY)) if (screenX - oldX)*(screenX - oldX) + (screenY - oldY)*(screenY - oldY) < 12 =>
          clickAt(screenX, screenY)
          true
        case _ =>
          false
      }
    else false
  }

  def clickAt(x: Int, y: Int): Unit
}

trait GameInputProcessor extends ScreenInputProcessor {
  
  protected def gameController: GameController
  override def screen: ScramblScreen
  
  def clickAt(x: Int, y: Int) {
    val h = gameController.at(x, y)
    gameController.clickAction(h)
  }
}

class DefaultScreenInputProcessor(val screen: ScramblScreen,
                                  protected val gameController: GameController)
  extends GameInputProcessor

trait StdController extends GameController {
  def init(): Unit
  def continuousActions(): Unit
  def inputProcessor: InputProcessor
  def menu: Menu
}

class DefaultScramblController(override val model: FlakeModel,
                               override val screen: ScramblScreen)
    extends StdController
    with FlakeGameController {

  override val inputProcessor: InputProcessor = new DefaultScreenInputProcessor(screen, this)

  import widgets.skin
  val menu: Menu = new DefaultMenu

  def init(): Unit = {}

  def continuousActions() = {}

  def clickAction(hexa: LivingHexagon) {
    if (!model.locked) {
      onChange(model.toggle(hexa.neighbours.toSet))
      if (model.success) {
        nextLevel()
      }
    }
  }
}