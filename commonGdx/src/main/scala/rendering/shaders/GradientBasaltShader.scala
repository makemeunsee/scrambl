package rendering.shaders

import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh
import noise.SimplexNoise
import rendering.Color
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import rendering.colorToFloats

object GradientBasaltShader extends GdxShaderModule[Hexagon] with LavaBasalt {
  
  private class NoisedColor(r: Float, g: Float, b: Float, n: Float)
    extends GdxColor(r + n/1.2f * r/1.7f,
                     g + n/1.2f * g/1.7f,
                     b + n/1.2f * b/1.7f,
                     1f)
    
  private def defaultNoisifier(color: Color, hex: Hexagon): Color = {
    val (r, g, b) = colorToFloats(color)
    val n = SimplexNoise.noise(hex.x/2f, hex.y/2f).toFloat
    new NoisedColor(r, g, b, n)
  }
  
  protected def attributes = Seq(
      VertexAttribute.Position(),
      VertexAttribute.Color(),
      new VertexAttribute(Usage.Generic, 2, "a_center"),
      new VertexAttribute(Usage.Generic, 3, "barycentric"))
  
  def verticeAndIndice(h: Hexagon) = {
    val color = defaultNoisifier(new GdxColor(basaltColor), h)
    val c = color.toFloatBits
    val cB = color.add(0.07f, 0.07f, 0.07f, 0).toFloatBits
    
    (Array[Float](
      h.center.x,    h.center.y,    0, cB, h.center.x, h.center.y, 1, 0, 0,
      h.points(0).x, h.points(0).y, 0, c,  h.center.x, h.center.y, 0, 1, 0,
      h.points(1).x, h.points(1).y, 0, c,  h.center.x, h.center.y, 0, 0, 1,
      h.points(2).x, h.points(2).y, 0, c,  h.center.x, h.center.y, 0, 1, 0,
      h.points(3).x, h.points(3).y, 0, c,  h.center.x, h.center.y, 0, 0, 1,
      h.points(4).x, h.points(4).y, 0, c,  h.center.x, h.center.y, 0, 1, 0,
      h.points(5).x, h.points(5).y, 0, c,  h.center.x, h.center.y, 0, 0, 1),
    Array[Short](0, 1, 2, 3, 4, 5, 6, 1))
  }
  
  def floatsPerVertex = -1 // unsupported

  protected def reasonableVerticeAndIndice(hexas: Seq[_ <: Hexagon]) = throw new Error("unsupported")
  
  val vertexShader = """#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
attribute vec4 a_position;
varying vec3 vBC;
attribute vec3 barycentric;
attribute vec4 a_color;
attribute vec2 a_center;
uniform mat4 u_worldView;
uniform float u_alpha;
varying vec4 v_color;

void main()
{
  vBC = barycentric;
  v_color.rgb = a_color.rgb;
  v_color.a = u_alpha;
  vec4 v = a_position;
  v.x = a_center.x + u_alpha * (v.x - a_center.x);
  v.y = a_center.y + u_alpha * (v.y - a_center.y);
  gl_Position = u_worldView * v;
}"""
    
  val fragmentShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
precision mediump float;
precision mediump int;
#endif
varying vec3 vBC;
varying vec4 v_color;

float edgeFactor(){
  vec3 d = fwidth(vBC);
  vec3 a3 = smoothstep(vec3(0.0), d*0.8, vBC);
  return a3.x;
}

void main()
{
  float f = edgeFactor();
  gl_FragColor = vec4(v_color.r, v_color.g, v_color.b, f);
}"""
}