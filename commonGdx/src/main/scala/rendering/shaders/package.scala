package rendering

package object shaders {
  implicit def intToCol: Int => Color = new GdxColor(_)
  val GdxShadersPack = ShadersPack()
}