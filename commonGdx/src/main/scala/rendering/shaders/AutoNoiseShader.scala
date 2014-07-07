package rendering.shaders

import rendering.Color
import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import MeshMaker._
import rendering.GenericColor

object AutoNoiseShader { 
  
  val simplexNoiseGLSL = """//
// Description : Array and textureless GLSL 2D simplex noise function.
//      Author : Ian McEwan, Ashima Arts.
//  Maintainer : ijm
//     Lastmod : 20110822 (ijm)
//     License : Copyright (C) 2011 Ashima Arts. All rights reserved.
//               Distributed under the MIT License. See LICENSE file.
//               https://github.com/ashima/webgl-noise
// 

vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v) {
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
// First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

// Other corners
  vec2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
        + i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m ;
  m = m*m ;

// Gradients: 41 points uniformly over a line, mapped onto a diamond.
// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;

// Normalise gradients implicitly by scaling m
// Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

// Compute final noise value at P
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}"""
}

case class AutoNoiseShader(color0: GenericColor,
                         color1: GenericColor,
                         blendingRate: Float = 1f,
                         border: BorderMode = NoFX,
                         mode: SproutMode = NoFX,
                         cubic: Boolean = false)
    extends GdxShaderModule[Hexagon] {
  
  protected def attributes = {
    Seq(VertexAttribute.Position(),
        new VertexAttribute(Usage.Generic, 2, "a_center"),
        new VertexAttribute(Usage.Generic, 2, "a_hexacoords"),
        new VertexAttribute(Usage.Generic, 3, "a_barycentric"),
        new VertexAttribute(Usage.Generic, 1, "a_tier"))
  }

  private def createVertice(h: Hexagon) = {
    Seq[Float](
          h.center.x,    h.center.y,    0, h.center.x, h.center.y, h.x, h.y, 1, 0, 0, 0,  
          h.points(0).x, h.points(0).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 1, 0, 1,
          h.points(1).x, h.points(1).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 0, 1, 0,
          h.points(2).x, h.points(2).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 1, 0, -1,
          h.points(3).x, h.points(3).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 0, 1, 0,
          h.points(4).x, h.points(4).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 1, 0, 0,
          h.points(5).x, h.points(5).y, 0, h.center.x, h.center.y, h.x, h.y, 0, 0, 1, 0)
  }

  def verticeAndIndice(h: Hexagon) = {
    (Array(createVertice(h): _*),
     Array[Short](0, 1, 2, 3, 4, 5, 6, 1))
  }
  
  def floatsPerVertex = 11
  
  protected def reasonableVerticeAndIndice(hexas: Seq[_ <: Hexagon]) = {
    val count = hexas.size

    val verticeCount = count * verticePerHexa
    val floatCount = verticeCount * floatsPerVertex
    val indiceCount = count * indicePerHexa

    val vertice = new Array[Float](floatCount)
    val indice = new Array[Short](indiceCount)

    hexas.zipWithIndex.map { case (h, i) =>

      val iOffset = i * indicePerHexa
      val vOffset = i * verticePerHexa
      val fOffset = vOffset * floatsPerVertex

      Array(createVertice(h): _*).copyToArray(vertice, fOffset, floatsPerHexa)

      Array[Short](0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 6, 0, 6, 1).map(j  => (j + vOffset).toShort).copyToArray(indice, iOffset, indicePerHexa)
    }
    (vertice, indice)
  }
  
  private def applyAgony = mode match {
    case Shrinking =>
      """|v.x = a_center.x + u_agony_alpha * (v.x - a_center.x);
         |v.y = a_center.y + u_agony_alpha * (v.y - a_center.y);""".stripMargin

    case Fading =>
      """v_color.a = u_agony_alpha * v_color.a;"""

    case _ =>
      ""
  }

  val vertexShader = s"""#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
attribute vec4 a_position;
attribute vec2 a_center;
attribute vec2 a_hexacoords;
attribute vec3 a_barycentric;
varying vec3 v_barycentric;
attribute float a_tier;
varying float v_tier;
varying vec4 v_color;
uniform mat4 u_worldView;
uniform float u_time;
uniform float u_agony_alpha;

float bounded(const float f) {
  return max(min(f, 1.0), 0.0);
}
  
${AutoNoiseShader.simplexNoiseGLSL}

void main() {
  float n0 = snoise(vec2(a_hexacoords.x * ${color0.noiseScalingX} + ${color0.offsetX}, a_hexacoords.y * ${color0.noiseScalingY} + ${color0.offsetY}));
  float n1 = snoise(vec2(a_hexacoords.x * ${color1.noiseScalingX} + ${color1.offsetX}, a_hexacoords.y * ${color1.noiseScalingY} + ${color1.offsetY}));
  vec4 col0 = vec4(bounded(${color0.baseColor.r} * (1.0 + n0 * ${color0.noiseCoeffs._1})),
                   bounded(${color0.baseColor.g} * (1.0 + n0 * ${color0.noiseCoeffs._2})),
                   bounded(${color0.baseColor.b} * (1.0 + n0 * ${color0.noiseCoeffs._3})),
                   ${color0.baseColor.a});
  vec4 col1 = vec4(bounded(${color1.baseColor.r} * (1.0 + n1 * ${color1.noiseCoeffs._1})),
                   bounded(${color1.baseColor.g} * (1.0 + n1 * ${color1.noiseCoeffs._2})),
                   bounded(${color1.baseColor.b} * (1.0 + n1 * ${color1.noiseCoeffs._3})),
                   ${color1.baseColor.a});
  // center shade
  if (a_position.xy == a_center) {
    col0 = vec4(bounded(col0.r + ${color0.shadingCoeffs._1}),
                bounded(col0.g + ${color0.shadingCoeffs._2}),
                bounded(col0.b + ${color0.shadingCoeffs._3}),
                col0.a);
    col1 = vec4(bounded(col1.r + ${color1.shadingCoeffs._1}),
                bounded(col1.g + ${color1.shadingCoeffs._2}),
                bounded(col1.b + ${color1.shadingCoeffs._3}),
                col1.a);
  }
  v_tier = a_tier;
  v_barycentric = a_barycentric;
  float blendAlpha = sin($blendingRate * u_time / 2000.0) / 2.0 + 0.5;
  v_color = blendAlpha * col0 + (1.0 - blendAlpha) * col1;
  vec4 v = a_position;
  $applyAgony
  gl_Position = u_worldView * v;
}"""

  private def withEdge(color: Color, thickness: Float) = s"""float f = edgeFactor($thickness);
  gl_FragColor = mix(vec4(${color.r}, ${color.g}, ${color.b}, ${color.a}), tierColor, f);"""

  private def edgeLess ="""gl_FragColor = tierColor;"""

  private def applyEdge = border match {
    case Border(c, t) => withEdge(c, t)
    case _ => edgeLess
  }

  private def tierColor =
    if (cubic) """if (v_tier < 0.0) {
    tierColor = vec4(v_color.x -0.07, v_color.y -0.07, v_color.z -0.07, v_color.a);
  } else if (v_tier > 0.0) {
    tierColor = vec4(v_color.x +0.07, v_color.y +0.07, v_color.z +0.07, v_color.a);
  } else {
    tierColor = v_color;
  }"""
    else """tierColor = v_color;"""

  val fragmentShader = s"""#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
precision mediump float;
precision mediump int;
#endif
varying vec3 v_barycentric;
varying float v_tier;
varying vec4 v_color;

float edgeFactor(const float thickness){
  vec3 d = fwidth(v_barycentric);
  vec3 a3 = smoothstep(vec3(0.0), d*thickness, v_barycentric);
  return a3.x;
}

void main()
{
  vec4 tierColor;
  $tierColor
  $applyEdge
}"""
}