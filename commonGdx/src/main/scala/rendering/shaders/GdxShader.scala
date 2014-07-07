package rendering.shaders

import com.badlogic.gdx.graphics.glutils.ShaderProgram

trait GdxShader extends Shader {
  def createShaderProgram = new ShaderProgram(vertexShader, fragmentShader)
}