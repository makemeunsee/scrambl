package rendering.screen

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import rendering.shaders._
import world2d.LivingHexagon
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.GL20
import models.GridModel

trait ForegroundScreen extends ScreenTrait[LivingHexagon] {

  protected def model: GridModel[LivingHexagon]

  protected def createForegroundBlocks = worldHexas

  private var (foregroundBlocks, hexToId) = createForegroundBlocks

  def updateForegroundBlocks() {
    val tmp = createForegroundBlocks
    foregroundBlocks = tmp._1
    hexToId = tmp._2
  }
  
  private var blockShaderTool: GenericShaderModule = GdxShadersPack.values(0).blockShader
  
  def blockGenericShader: GenericShaderModule = blockShaderTool
  
  private var blockShader = blockShaderTool.createShaderProgram
  println(blockShader.getLog)
  private var blockMeshes: Seq[Mesh] = Seq.empty
  
  def setBlockShader(newBlockShaderTool: GenericShaderModule, recreateMesh: Boolean = true) {
    blockShaderTool = newBlockShaderTool

    blockShader.dispose()
    blockShader = newBlockShaderTool.createShaderProgram
    println(blockShader.getLog)

    if (recreateMesh) {
      blockMeshes foreach (_.dispose())
      blockMeshes = newBlockShaderTool.createMeshes(foregroundBlocks)
      updateBlocks(model.onGrid)
    }
    
    println(s"block shader: $newBlockShaderTool")
  }
  
  def updateBlocks(blocks: Set[_ <: LivingHexagon]) = /*perf.perfed("updateBlocks")*/ {
    val lifeDeathOffset = ShaderModule.lifeDeathOffset
    for ( block <- blocks ) {
      val id = hexToId(block)
      if (id > -1 && id < foregroundBlocks.size) {
        val meshId = id / MeshMaker.hexaPerMesh
        val mesh = blockMeshes(meshId)
        val idInMesh = id % MeshMaker.hexaPerMesh
        val baseOffset = idInMesh * blockShaderTool.floatsPerHexa + lifeDeathOffset
        updateTimesOfHexaInMesh(block, mesh, baseOffset)
      }
    }
  }

  protected def updateTimesOfHexaInMesh(hexa: LivingHexagon, mesh: Mesh, offset: Int) {
    (0 until MeshMaker.verticePerHexa)
      .foreach { i =>
      val vOffset = offset + i * ShaderModule.floatsPerVertex
      mesh.updateVertices(vOffset, Array[Float](hexa.birthtime, hexa.deathtime))
    }
  }

  def renderBlocks(delta: Float, now: Float, shader: ShaderProgram = blockShader) {

    shader.begin()
    shader.setUniformMatrix("u_worldView", camera.combined)
    shader.setUniformf("u_time", now)
  
    blockMeshes foreach (_.render(shader, GL20.GL_TRIANGLES))

    shader.end()
  }
}