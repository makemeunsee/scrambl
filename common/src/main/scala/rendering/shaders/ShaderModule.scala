package rendering.shaders

import rendering.shaders.MeshMaker._
import rendering.shaders.Shader._
import world2d.Hexagon
import rendering.Color
import rendering.GenericColor

sealed trait BorderMode
case class Border(color: Color, thickness: Float = 0.8f) extends BorderMode

sealed trait SproutMode
case object Shrinking extends SproutMode
case object Fading extends SproutMode
case class  Wither(shift: Float = 1f) extends SproutMode

sealed trait HighlightMode
case class Pulsating(rate: Float = 1, amplitude: Float = 0.25f, shift: Float = 0f) extends HighlightMode
case class Blending(rate: Float = 1, amplitude: Float = 0.25f, shift: Float = 0f) extends HighlightMode
case class Shaking(rate: Float = 10, amplitude: (Float, Float) = (2f, 1f)) extends HighlightMode
case class PulsatingShaking(rateS: Float = 10, amplitudeS: (Float, Float) = (2f, 1f),
                            rateP: Float = 1, amplitudeP: Float = 0.25f, shiftP: Float = 0f) extends HighlightMode
case class Squeezing(restDuration: Float = 4.5f, squeezeDuration: Float = 0.1f, amplitude: Float = 0.2f) extends HighlightMode
case object Dead extends HighlightMode

case object NoFX extends HighlightMode with BorderMode with SproutMode

object ShaderModule {
  val floatsPerVertex = 13
  val lifeDeathOffset = 11
}

trait ShaderModule[H <: Hexagon] extends Shader with MeshMaker[H] {
  
  def color0: GenericColor
  def color1: GenericColor
  
  def defaultBirth: H => Float
  def defaultDeath: H => Float

  private def setVertice(arr: Array[Float],
                         offset: Int,
                         h: Hexagon,
                         nc0: Float,       // noised color 1
                         nc1: Float,       // noised color 2
                         sc0: Float,       // noised center color 1
                         sc1: Float,       // noised center color 2
                         birth: Float,     // birth time
                         death: Float) = { // death time
    arr(offset) = h.center.x
    arr(offset + 1) = h.center.y
    arr(offset + 2) = 0
    arr(offset + 3) = h.center.x
    arr(offset + 4) = h.center.y
    arr(offset + 5) = 1
    arr(offset + 6) = 0
    arr(offset + 7) = 0
    arr(offset + 8) = 0
    arr(offset + 9) = sc0
    arr(offset + 10) = sc1
    arr(offset + 11) = birth
    arr(offset + 12) = death
    arr(offset + 13) = h.points(0).x
    arr(offset + 14) = h.points(0).y
    arr(offset + 15) = 0
    arr(offset + 16) = h.center.x
    arr(offset + 17) = h.center.y
    arr(offset + 18) = 0
    arr(offset + 19) = 1
    arr(offset + 20) = 0
    arr(offset + 21) = 1
    arr(offset + 22) = nc0
    arr(offset + 23) = nc1
    arr(offset + 24) = birth
    arr(offset + 25) = death
    arr(offset + 26) = h.points(1).x
    arr(offset + 27) = h.points(1).y
    arr(offset + 28) = 0
    arr(offset + 29) = h.center.x
    arr(offset + 30) = h.center.y
    arr(offset + 31) = 0
    arr(offset + 32) = 0
    arr(offset + 33) = 1
    arr(offset + 34) = 0
    arr(offset + 35) = nc0
    arr(offset + 36) = nc1
    arr(offset + 37) = birth
    arr(offset + 38) = death
    arr(offset + 39) = h.points(2).x
    arr(offset + 40) = h.points(2).y
    arr(offset + 41) = 0
    arr(offset + 42) = h.center.x
    arr(offset + 43) = h.center.y
    arr(offset + 44) = 0
    arr(offset + 45) = 1
    arr(offset + 46) = 0
    arr(offset + 47) = -1
    arr(offset + 48) = nc0
    arr(offset + 49) = nc1
    arr(offset + 50) = birth
    arr(offset + 51) = death
    arr(offset + 52) = h.points(3).x
    arr(offset + 53) = h.points(3).y
    arr(offset + 54) = 0
    arr(offset + 55) = h.center.x
    arr(offset + 56) = h.center.y
    arr(offset + 57) = 0
    arr(offset + 58) = 0
    arr(offset + 59) = 1
    arr(offset + 60) = 0
    arr(offset + 61) = nc0
    arr(offset + 62) = nc1
    arr(offset + 63) = birth
    arr(offset + 64) = death
    arr(offset + 65) = h.points(4).x
    arr(offset + 66) = h.points(4).y
    arr(offset + 67) = 0
    arr(offset + 68) = h.center.x
    arr(offset + 69) = h.center.y
    arr(offset + 70) = 0
    arr(offset + 71) = 1
    arr(offset + 72) = 0
    arr(offset + 73) = 0
    arr(offset + 74) = nc0
    arr(offset + 75) = nc1
    arr(offset + 76) = birth
    arr(offset + 77) = death
    arr(offset + 78) = h.points(5).x
    arr(offset + 79) = h.points(5).y
    arr(offset + 80) = 0
    arr(offset + 81) = h.center.x
    arr(offset + 82) = h.center.y
    arr(offset + 83) = 0
    arr(offset + 84) = 0
    arr(offset + 85) = 1
    arr(offset + 86) = 0
    arr(offset + 87) = nc0
    arr(offset + 88) = nc1
    arr(offset + 89) = birth
    arr(offset + 90) = death
  }

  private def setIndice(arr: Array[Short], offset: Int, centerVertexOffset: Short) {
    arr(offset) = centerVertexOffset
    arr(offset+1) = (1+centerVertexOffset).toShort
    arr(offset+2) = (2+centerVertexOffset).toShort
    arr(offset+3) = centerVertexOffset
    arr(offset+4) = (2+centerVertexOffset).toShort
    arr(offset+5) = (3+centerVertexOffset).toShort
    arr(offset+6) = centerVertexOffset
    arr(offset+7) = (3+centerVertexOffset).toShort
    arr(offset+8) = (4+centerVertexOffset).toShort
    arr(offset+9) = centerVertexOffset
    arr(offset+10) = (4+centerVertexOffset).toShort
    arr(offset+11) = (5+centerVertexOffset).toShort
    arr(offset+12) = centerVertexOffset
    arr(offset+13) = (5+centerVertexOffset).toShort
    arr(offset+14) = (6+centerVertexOffset).toShort
    arr(offset+15) = centerVertexOffset
    arr(offset+16) = (6+centerVertexOffset).toShort
    arr(offset+17) = (1+centerVertexOffset).toShort
  }
  
  // single hexa mesh functions
  
  def verticeAndIndice(h: H): (Array[Float], Array[Short]) =
    verticeAndIndice(defaultBirth, defaultDeath)(h)
    
  def verticeAndIndice(birth: H => Float, death: H => Float)(h: H): (Array[Float], Array[Short]) = {
    val nc0 = color0.noised(h).toFloatBits
    val nc1 = color1.noised(h).toFloatBits
    val sc0 = color0.shaded(h).toFloatBits
    val sc1 = color1.shaded(h).toFloatBits

    val vertice = new Array[Float](verticePerHexa * floatsPerVertex)
    setVertice(vertice, 0, h, nc0, nc1, sc0, sc1, birth(h), death(h))

    val indice = new Array[Short](indicePerHexa)
    setIndice(indice, 0, 0)

    (vertice, indice)
  }
  
  // many hexas mesh functions
  
  def floatsPerVertex = ShaderModule.floatsPerVertex
  
  // triangles mesh for lots of hexagons
  // precondition: hexas.size <= indexCountMax / verticePerHexa
  protected def reasonableVerticeAndIndice(hexas: Seq[_ <: H]): (Array[Float], Array[Short]) =
    reasonableVerticeAndIndice(defaultBirth, defaultDeath)(hexas)
  
  protected def reasonableVerticeAndIndice(birth: H => Float, death: H => Float)(hexas: Seq[_ <: H]) = {
    val count = hexas.size

    val verticeCount = count * verticePerHexa
    val floatCount = verticeCount * floatsPerVertex
    val indiceCount = count * indicePerHexa

    val vertice = new Array[Float](floatCount)
    val indice = new Array[Short](indiceCount)

    hexas.zipWithIndex.foreach { case (h, i) =>

      val nc0 = color0.noised(h).toFloatBits
      val nc1 = color1.noised(h).toFloatBits
      val sc0 = color0.shaded(h).toFloatBits
      val sc1 = color1.shaded(h).toFloatBits

      val iOffset = i * indicePerHexa
      val vOffset = i * verticePerHexa
      val fOffset = vOffset * floatsPerVertex

      setVertice(vertice, fOffset, h, nc0, nc1, sc0, sc1, birth(h), death(h))
      setIndice(indice, iOffset, vOffset.toShort)
    }

    (vertice, indice)
  }

  // shaders related code

  def hexagonWidth: Float
  
  def blendingRate: Float = 1f
  def border: BorderMode = NoFX
  def sprouting: SproutMode = NoFX
  def highlighting: HighlightMode = NoFX
  def cubic: Boolean = false

  private val alphaCompute =
    s"""|float timeLive = u_time - a_birthtime;
        |float timeRemaining = a_deathtime - u_time;
        |float alpha = 0.0;
        |if ( u_time > a_birthtime && u_time < a_deathtime ) {
        |  if ( timeRemaining < $transitionTime && timeLive < $transitionTime ) {
        |    alpha = min(timeRemaining/$transitionTime, timeLive/$transitionTime);
        |  } else if ( timeRemaining < $transitionTime ) {
        |    alpha = timeRemaining/$transitionTime;
        |  } else if ( timeLive < $transitionTime ) {
        |    alpha = timeLive/$transitionTime;
        |  } else {
        |    alpha = 1.0;
        |  }
        |}""".stripMargin
    
  private val twoPiBy1000 = 2*math.Pi / 1000f

  private val highlight = highlighting match {
    case Dead =>
      s"""|v_color = vec4(1.0, 1.0, 1.0, v_color.a + v_color.a) - v_color;
          |v.x = v.x - 0.5 * (v.x - a_center.x);
          |v.y = v.y - 0.5 * (v.y - a_center.y);""".stripMargin
    case Pulsating(rate, amplitude, shift) =>
      s"""|float hAlpha = sin((u_time - a_birthtime) * $rate * $twoPiBy1000);
          |v.x = v.x + (hAlpha * $amplitude + $shift) * (v.x - a_center.x);
          |v.y = v.y + (hAlpha * $amplitude + $shift) * (v.y - a_center.y);""".stripMargin
    case Shaking(rate, (amplitudeX, amplitudeY)) =>
      s"""|float alphaShake = sin((u_time - a_birthtime) * $rate * $twoPiBy1000);
          |v.x = v.x + alphaShake * $amplitudeX;
          |v.y = v.y + alphaShake * $amplitudeY;""".stripMargin
    case PulsatingShaking(rateS, (amplitudeX, amplitudeY), rateP, amplitudeP, shiftP) =>
      s"""|float alphaShake = sin((u_time - a_birthtime) * $rateS * $twoPiBy1000);
          |float alphaPulse = sin((u_time - a_birthtime) * $rateP * $twoPiBy1000);
          |v.x = v.x + alphaShake * $amplitudeX + (alphaPulse * $amplitudeP + $shiftP) * (v.x - a_center.x);
          |v.y = v.y + alphaShake * $amplitudeY + (alphaPulse * $amplitudeP + $shiftP) * (v.y - a_center.y);""".stripMargin
    case Blending(rate, amplitude, shift) =>
      s"""|float hAlpha = sin((u_time - a_birthtime) * $rate * $twoPiBy1000);
          |v_color.a = (0.5 + $shift + hAlpha * $amplitude) * v_color.a;""".stripMargin
    case Squeezing(restD, squeezeD, amplitude) =>
      val period = restD + squeezeD
      s"""|float life = (u_time - a_birthtime) / 1000.0;
          |float state = life - $period * floor(life / $period);
          |if (state > $restD) {
          |  v.x = v.x - $amplitude * (v.x - a_center.x);
          |  v.y = v.y - $amplitude * (v.y - a_center.y);
          |}""".stripMargin
    case NoFX => ""
  }

  private def sprout = sprouting match {
    case Shrinking =>
      s"""|$alphaCompute
          |v.x = a_center.x + alpha * (v.x - a_center.x);
          |v.y = a_center.y + alpha * (v.y - a_center.y);""".stripMargin

    case Fading =>
      s"""|$alphaCompute
          |v_color.a = alpha * v_color.a;""".stripMargin
          
    case Wither(shiftRatio) =>
      val shift = hexagonWidth * shiftRatio * 0.5f
      val piBy3 = 1.04719755f
      s"""|$alphaCompute
          |float xShift = sin(mod(a_birthtime, 6.0) * $piBy3) * $shift;
          |float yShift = cos(mod(a_birthtime, 6.0) * $piBy3) * $shift;
          |v_color = (1.0 - alpha) * vec4(1.0 - v_color.r, 1.0 - v_color.g, 1.0 - v_color.b, v_color.a) + alpha * v_color;
          |v.x = v.x - (1.0 - alpha) * (0.3 * (v.x - a_center.x) + xShift);
          |v.y = v.y - (1.0 - alpha) * (0.3 * (v.y - a_center.y) + yShift);
          |""".stripMargin

    case NoFX =>
      ""
  }

  val vertexShader = s"""#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
attribute vec4 a_position;
attribute vec2 a_center;
attribute vec3 a_barycentric;
varying vec3 v_barycentric;
attribute float a_tier;
varying float v_tier;
attribute vec4 a_color0;
attribute vec4 a_color1;
varying vec4 v_color;
uniform mat4 u_worldView;
uniform float u_time;
attribute float a_birthtime;
attribute float a_deathtime;

void main()
{
  v_barycentric = a_barycentric;
  v_tier = a_tier;
  float blendAlpha = sin($blendingRate * u_time / 2000.0) / 2.0 + 0.5;
  v_color = blendAlpha * a_color0 + (1.0 - blendAlpha) * a_color1;
  vec4 v = a_position;
  $sprout
  $highlight
  gl_Position = u_worldView * v;
}"""

  private def withEdge(color: Color, thickness: Float) = s"""float f = edgeFactor($thickness);
  gl_FragColor = mix(vec4(${color.r}, ${color.g}, ${color.b}, ${color.a} * tierColor.a), tierColor, f);"""

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
