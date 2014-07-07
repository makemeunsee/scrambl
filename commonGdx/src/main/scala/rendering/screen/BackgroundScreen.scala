package rendering.screen

import rendering.shaders.{CubicLavaShader, GdxShaderModule}
import com.badlogic.gdx.graphics.{Mesh, GL20}
import com.badlogic.gdx.Gdx
import world2d.Hexagon

trait BackgroundScreen[ST <: GdxShaderModule[H], H <: Hexagon] extends ScreenTrait[H] {
  
  private var backgroundShader = CubicLavaShader.createShaderProgram // default value
  
  private var backgroundMeshes: Iterable[Mesh] = Seq.empty

  def setBackgroundShader(newShaderTool: ST, recreateMesh: Boolean = true) = /*perf.perfed(s"setBackgroundShader, $recreateMesh")*/ {
    backgroundShader.dispose()
    backgroundShader = newShaderTool.createShaderProgram
    println(backgroundShader.getLog)

    if (recreateMesh) {
      backgroundMeshes foreach (_.dispose())
      backgroundMeshes = newShaderTool.createMeshes(worldHexas._1)
    }

    println(s"background shader: $newShaderTool")
  }

  def renderBackground(delta: Float, now: Float) {

    Gdx.gl20.glEnable(GL20.GL_BLEND)
    Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // meshes rendering
    // lava background
    backgroundShader.begin()
    backgroundShader.setUniformMatrix("u_worldView", camera.combined)
    backgroundShader.setUniformf("u_time", now)
    backgroundMeshes foreach (_.render(backgroundShader, GL20.GL_TRIANGLES))
    backgroundShader.end()
  }

}