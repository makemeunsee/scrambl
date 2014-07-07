package rendering

import com.badlogic.gdx.graphics.Texture
import scala.collection.immutable.SortedMap
import WorldTexture._
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.glutils.ShaderProgram

object WorldTexture {
  val textureSize = 1024
  
  def empty: WorldTexture = new WorldTextureImpl(Map.empty)

  // SHADERS
  
  private val lavaTextureVertexShader = """#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute float f_alpha;
uniform mat4 u_worldView;
varying vec2 v_texCoords;
    
void main()
{
  v_texCoords = a_texCoord0;
  gl_Position =  u_worldView * a_position;
}"""
  private val lavaTextureFragmentShader = """#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
varying vec2 v_texCoords; 
uniform float u_cycle_alpha;

void main()
{
  vec4 t0 = texture2D(u_texture1, v_texCoords);
  vec4 t1 = texture2D(u_texture2, v_texCoords);
  gl_FragColor = t0 * u_cycle_alpha + t1 * (1.0 - u_cycle_alpha);
}"""
    
  val lavaTextureShader = new ShaderProgram(lavaTextureVertexShader, lavaTextureFragmentShader)
  println(s"lavaTextureShader logs: ${lavaTextureShader.getLog}")
}

case class LavaTexture(textureA: Texture, textureB: Texture, x: Int, y: Int, scale: Float) { lavaText =>

  val mesh = createMesh
  
  def bounds = {
    val unit = textureSize * scale
    (x * unit, y * unit, unit, unit)
  }

  private def createMesh = {
    val (wX, wY, w, h) = bounds
    val mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0))
    mesh.setVertices(Array[Float](
      wX,   wY,   0, 0, 0,
      wX+w, wY,   0, 1, 0,
      wX+w, wY+h, 0, 1, 1,
      wX,   wY+h, 0, 0, 1))
    mesh.setIndices(Array[Short](0, 1, 2, 2, 3, 0))
    mesh
  }
}

trait WorldTexture {

  // ordered map of lava textures by optimal zoom level
  def allTextures: Map[Float, Seq[LavaTexture]]
  
  def addTextures(originalZoomLevel: Float, textures: Seq[LavaTexture]): WorldTexture

  def texturesAt(zoomLevel: Float, viewXMin: Float, viewXMax: Float, viewYMin: Float, viewYMax: Float): Seq[LavaTexture] = {
    if (allTextures.isEmpty) throw new Error("no texture available")
    
    val unfilteredTextures = allTextures.filter(_._1 <= zoomLevel).lastOption match {
      case Some((_, seq)) => seq
      case None           => allTextures.head._2
    }
    
    val unit = unfilteredTextures.head.scale * textureSize
    val (xMin, xMax, yMin, yMax) = ((viewXMin / unit).floor, (viewXMax / unit).ceil, (viewYMin / unit).floor, (viewYMax / unit).ceil)
    unfilteredTextures.filter( t => t.x < xMax && t.x >= xMin && t.y < yMax && t.y >= yMin)
  }
}

private class WorldTextureImpl(val allTextures: Map[Float, Seq[LavaTexture]]) extends WorldTexture {
  
  def addTextures(originalZoomLevel: Float, textures: Seq[LavaTexture]): WorldTexture =
    new WorldTextureImpl(SortedMap(allTextures.toSeq: _*) + ((originalZoomLevel, textures)))

}