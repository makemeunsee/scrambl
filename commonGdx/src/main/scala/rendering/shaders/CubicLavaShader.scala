package rendering.shaders

import rendering.Color
import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import noise.SimplexNoise
import rendering.colorToFloats

import MeshMaker._

object CubicLavaShader extends GdxShaderModule[Hexagon] with LavaBasalt {

  val hotRnd = 504
  val coldRnd = 128
  val colorDiff = 0.07f
  
  def lavaNoisifier(color: Color, n: Float): Color = {
    val (r, g, _) = colorToFloats(color)
    new GdxColor(r + n * r * 0.33f,
                 g + n * g * 0.5f,
                 0,
                 1f)
  }
  
  def verticeAndIndice(h: Hexagon) = throw new Error("unsupported")
  
  def floatsPerVertex = 6
  
  protected def attributes: Seq[VertexAttribute] = Seq(
      VertexAttribute.Position(),
      VertexAttribute.Color(),
      new VertexAttribute(Usage.ColorPacked, 4, "a_hot_color"),
      new VertexAttribute(Usage.Generic, 1, "a_tier"))
      
  protected def reasonableVerticeAndIndice(hexas: Seq[_ <: Hexagon]) = {
    
    val count = hexas.size
    
    val verticeCount = count * verticePerHexa
    val floatCount = verticeCount * floatsPerVertex
    val indiceCount = count * indicePerHexa
    
    val vertice = new Array[Float](floatCount)
    val indice = new Array[Short](indiceCount)
    
    val cLava = new GdxColor(colderLavaBaseColor)
    val hLava = new GdxColor(hotterLavaBaseColor)
    
    hexas.zipWithIndex.map { case (h, i) =>
      
      val iOffset = i * indicePerHexa
      val vOffset = i * verticePerHexa
      val fOffset = vOffset * floatsPerVertex
      
      val coldNoise = SimplexNoise.noise(h.x*noiseScaling + coldRnd, h.y*noiseScaling).toFloat
      val hotNoise = SimplexNoise.noise(h.x*noiseScaling + hotRnd, h.y*noiseScaling).toFloat
      val c1 = lavaNoisifier(cLava, coldNoise).toFloatBits
      val color2 = lavaNoisifier(hLava, hotNoise).toFloatBits
      
      Array[Float](
        h.center.x,    h.center.y,    0, c1, color2, 0,
        h.points(0).x, h.points(0).y, 0, c1, color2, 1,
        h.points(1).x, h.points(1).y, 0, c1, color2, 0,
        h.points(2).x, h.points(2).y, 0, c1, color2, -1,
        h.points(3).x, h.points(3).y, 0, c1, color2, 0,
        h.points(4).x, h.points(4).y, 0, c1, color2, 0,
        h.points(5).x, h.points(5).y, 0, c1, color2, 0)
      .copyToArray(vertice, fOffset, floatsPerHexa)
        
      Array[Short](0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 6, 0, 6, 1).map(j  => (j + vOffset).toShort).copyToArray(indice, iOffset, indicePerHexa)
    }
    (vertice, indice)
  }
  
  val vertexShader = """#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
attribute vec4 a_position;
attribute vec4 a_color; //cold color
attribute vec4 a_hot_color;
attribute float a_tier;
uniform mat4 u_worldView;
uniform float u_time;
varying vec4 v_color;
varying float v_tier;
    
void main()
{
  v_tier = a_tier;
  float blendAlpha = sin(u_time / 200.0) / 2.0 + 0.5;
  v_color = blendAlpha * a_color + (1.0 - blendAlpha) * a_hot_color;
  gl_Position =  u_worldView * a_position;
}"""
    
  val fragmentShader = """#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
varying vec4 v_color;
varying float v_tier;

void main()
{
  if (v_tier < 0.0) {
    gl_FragColor = vec4(v_color.x - 0.07, v_color.y - 0.07, 0.0, 1.0);
  } else if (v_tier > 0.0) {
    gl_FragColor = vec4(v_color.x + 0.07, v_color.y + 0.07, 0.0, 1.0);
  } else {
    gl_FragColor = v_color;
  }
}"""
}