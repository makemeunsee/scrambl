package rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.{ShaderProgram, FrameBuffer, ShapeRenderer}
import com.badlogic.gdx.graphics.{Mesh, OrthographicCamera, Pixmap, GL20}
import world2d.Hexagon
import world2d.LivingHexagon
import WorldTexture.textureSize
import com.badlogic.gdx.graphics.g2d.BitmapFont
import rendering.shaders.{GenericShaderModule, ShadersPack, GdxColor}
import GenericShaderModule._
import rendering.shaders.GdxShadersPack

class LavaBackground (lavaHexas: Seq[LivingHexagon], zoomMin: Float, zoomMax: Float, xViewMax: Int, yViewMax: Int) {
  
  if (zoomMin > zoomMax) throw new Error("Bad min/max zoom levels")

  val xMax = (xViewMax * zoomMax).toInt
  val yMax = (yViewMax * zoomMax).toInt
  
  val camera = new OrthographicCamera(textureSize, textureSize)
  def zoom = camera.zoom

  def asWorldTexture: WorldTexture = perf.perfed("world texture") {
    
    val lavaMeshes = GdxShadersPack.LavaBasaltGradient.backgroundShader.createMeshes(lavaHexas)

    val lavaShader = GdxShadersPack.LavaBasaltGradient.backgroundShader.createShaderProgram
    println(lavaShader.getLog)

    val wTex = WorldTexture.empty
    val zoomStream = Stream.from(0).map(i => math.max(zoomMin, 0.5f) * math.pow(2, i).toFloat)
    val zooms = zoomStream.take(zoomStream.indexWhere(z => z >= zoomMax) + 1)
    val res = zooms.foldLeft(wTex) { case (wt, zoomLevel) =>
      wt.addTextures(zoomLevel, texturesAt(lavaShader, lavaMeshes, zoomLevel))
    }

    lavaMeshes foreach (_.dispose())
    lavaShader.dispose()

    res
  }
  
  private def texturesAt(lavaShader: ShaderProgram, meshes: Iterable[Mesh], zoomLevel: Float): Seq[LavaTexture] = {
    camera.zoom = zoomLevel
    
    val texXIdMax = Stream.from(0).takeWhile(i => i * textureSize < xMax).last
    val texXIdMin = -texXIdMax - 1
    val texYIdMax = Stream.from(0).takeWhile(i => i * textureSize < yMax).last
    val texYIdMin = -texYIdMax - 1
    
    for (xId <- texXIdMin to texXIdMax;
         yId <- texYIdMin to texYIdMax) yield {
      camera.position.set((xId + 0.5f) * textureSize * zoom, (yId + 0.5f) * textureSize * zoom, 0)
      camera.update()
      LavaTexture(texture(lavaShader, meshes, 0, xId, yId),
          texture(lavaShader, meshes, 1, xId, yId),
          xId,
          yId,
          zoom)
    }
  }

  // debug info on textures
  private val batch = new SpriteBatch
  private val shaper = new ShapeRenderer
  shaper.setColor(com.badlogic.gdx.graphics.Color.GREEN)
  private val font = new BitmapFont
  private val textBatch = new SpriteBatch
  
  private def texture(lavaShader: ShaderProgram, meshes: Iterable[Mesh], alpha: Float, x: Int, y: Int) = {

    // RGB888 speeds up buffer creation but seems to up lot more memory... => crashes on portable devices
    val fb = new FrameBuffer(Pixmap.Format.RGB888, textureSize, textureSize, false)

    fb.begin()
    
    Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)
    Gdx.gl20.glLineWidth(math.ceil(1f/zoom).toInt)
    
    val viewHalfSize = textureSize * zoom / 2

    lavaShader.begin()
    lavaShader.setUniformMatrix("u_worldView", camera.combined)
    lavaShader.setUniformf("u_cycle_alpha", alpha)
    meshes foreach (_.render(lavaShader, GL20.GL_TRIANGLES))
    lavaShader.end()

    // debug infos on textures
    batch.setProjectionMatrix(camera.combined)
    shaper.setProjectionMatrix(camera.combined)
    batch.begin()
    shaper.begin(ShapeRenderer.ShapeType.Line)
    shaper.rect(camera.position.x - viewHalfSize, camera.position.y - viewHalfSize, textureSize * zoom, textureSize * zoom)
    shaper.end()
    batch.end()
    textBatch.begin()
    font.draw(textBatch, s"$zoom", textureSize / 2, textureSize / 2)
    textBatch.end()
    
//    // screenshot
//    val rgbaPixmap = ScreenUtils.getFrameBufferPixmap(0, 0, textureSize, textureSize)
//    val pixmap = new Pixmap(rgbaPixmap.getWidth, rgbaPixmap.getHeight, Pixmap.Format.RGB565)
//    pixmap.drawPixmap(rgbaPixmap, 0, 0)
//    val filename = s"texture_${zoom}_${x}_${y}_$textureSize."
//    //ETC1.encodeImagePKM(pixmap).write(new FileHandle(filename+"etc1"))
//    PixmapIO.writePNG(new FileHandle(filename+"png"), pixmap)
//    pixmap.dispose()
//    rgbaPixmap.dispose()
    
    fb.end()
    
    fb.getColorBufferTexture
  }

}