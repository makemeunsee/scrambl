package rendering.screen

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.{GL20, Mesh}
import scala.util.Random
import models.roaming.{RoamingModel, BranchModel, Intact, Dying, Playing, Dead, Won, Off}
import world2d.LivingHexagon
import models.DefaultModel
import rendering.shaders._
import GenericShaderModule._

class RoamingScreen(override val model: RoamingModel) extends ScramblScreen(model) {

  override protected def createForegroundBlocks = {
    val all = model.flake.flatten.toSeq
    (all, all.zipWithIndex.toMap)
  }
  
  private val baseShader = GdxShadersPack.LavaBasaltGradient.blockShader.copy(
    defaultBirth = h => h.birthtime,
    defaultDeath = h => h.deathtime,
    highlighting = NoFX)

  // ****** shaders for various parts of the screen, various state of the game ******* //

  private var failuresMeshes: Seq[Mesh] = Seq.empty
  private var nextStepsMeshes: Seq[Mesh] = Seq.empty

  val intactShader = baseShader.copy(
    highlighting = NoFX,
    sprouting = Shrinking)

  private val dyingShader = {
    val angle = Random.nextInt(360) * math.Pi / 180
    val radius = Random.nextInt(3) + 2
    baseShader.copy(
      highlighting = Shaking(6f, (radius * math.cos(angle).toFloat, radius * math.sin(angle).toFloat)),
      sprouting = Shrinking)
  }
  private val dyingShaderPg = dyingShader.createShaderProgram
  println(s"dyingShader ${dyingShaderPg.getLog}")

  private val nextStepsShader = baseShader.copy(
      highlighting = Blending(rate = 0.5f, amplitude = 0.3f, shift = -0.2f),
      defaultBirth = _ => DefaultModel.now,
      defaultDeath = _ => Float.MaxValue)
  private val nextStepsShaderPg = nextStepsShader.createShaderProgram
  println(s"nextStepsShader ${nextStepsShaderPg.getLog}")

  private val failuresShader = baseShader.copy(
    highlighting = NoFX,
    sprouting = Wither(0.6f))
  private val failuresShaderPg = failuresShader.createShaderProgram
  println(s"failuresShader ${failuresShaderPg.getLog}")


  private val victoryShader = baseShader.copy(
    highlighting = Squeezing(restDuration = (RoamingModel.flickerPeriod - BranchModel.hexBirthTimeStep * 2) / 1000f,
                             // highlights 2 successive hexas at a time
                             squeezeDuration = BranchModel.hexBirthTimeStep * 2 / 1000f,                  
                             amplitude = 0.2f),
    sprouting = Fading)
  private val victoryShaderPg = victoryShader.createShaderProgram
  println(s"victoryShader ${victoryShaderPg.getLog}")

  // ******* end shaders ********** //

  override def updateBlocks(blocks: Set[_ <: LivingHexagon]): Unit = /*perf.perfed("roaming screen update")*/ {
    super.updateBlocks(blocks)
    updateDead()
  }

  def updateDead() {
    failuresMeshes  = updateMeshes(failuresMeshes,  failuresShader,  model.dead)
    nextStepsMeshes = updateMeshes(nextStepsMeshes, nextStepsShader, model.nextSteps)
  }

  private def updateMeshes(oldMeshes: Seq[Mesh], newShader: GenericShaderModule, modelHexas: Seq[LivingHexagon]) = {
    oldMeshes map (_.dispose())
    newShader.createMeshes(modelHexas)
  }

  override def renderBlocks(delta: Float, now: Float, shaderPg: ShaderProgram = victoryShaderPg) {
    // failures always remain...
    failuresShaderPg.begin()
    failuresShaderPg.setUniformMatrix("u_worldView", camera.combined)
    failuresShaderPg.setUniformf("u_time", now)
    failuresMeshes map (_.render(failuresShaderPg, GL20.GL_TRIANGLES))
    failuresShaderPg.end()

    model.state match {
      // intro part 1, branches sprout
      case Intact =>
        super.renderBlocks(delta, now)

      // intro part 2, playable branches shake and die. ref branch just shakes
      case Dying =>
        super.renderBlocks(delta, now, dyingShaderPg)
        
      // active gameplay. highlight possible next steps. show ref and built branches.
      case Playing =>
        nextStepsShaderPg.begin()
        nextStepsShaderPg.setUniformMatrix("u_worldView", camera.combined)
        nextStepsShaderPg.setUniformf("u_time", now)
        nextStepsMeshes map (_.render(nextStepsShaderPg, GL20.GL_TRIANGLES))
        nextStepsShaderPg.end()

        super.renderBlocks(delta, now)

      // use victory shader for, hmm, nice effects
      case Won =>
        super.renderBlocks(delta, now, victoryShaderPg)

      case Off  => ()
      
      case Dead => () // only failures here
    }
  }
}
