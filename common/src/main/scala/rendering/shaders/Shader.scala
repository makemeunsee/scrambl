package rendering.shaders

import world2d.LivingHexagon.agonyDuration

object Shader {
  val transitionTime = agonyDuration
}

trait Shader {
  def vertexShader: String
  def fragmentShader: String
}