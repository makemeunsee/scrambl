package rendering.shaders

import rendering.GenericColor
import world2d.LivingHexagon
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage

object GenericShaderModule {
  import scala.language.implicitConversions
  implicit def fromShaderModule(sm: ShaderModule[LivingHexagon]): GenericShaderModule =
    GenericShaderModule(sm.color0, sm.color1,
        sm.blendingRate,
        sm.border,
        sm.sprouting, sm.highlighting,
        sm.cubic,
        sm.defaultBirth, sm.defaultDeath)
}

case class GenericShaderModule(
                         override val color0: GenericColor,
                         override val color1: GenericColor,
                         override val blendingRate: Float = 1f,
                         override val border: BorderMode = NoFX,
                         override val sprouting: SproutMode = NoFX,
                         override val highlighting: HighlightMode = NoFX,
                         override val cubic: Boolean = false,
                         override val defaultBirth: LivingHexagon => Float,
                         override val defaultDeath: LivingHexagon => Float)
  extends ShaderModule[LivingHexagon] with GdxShaderModule[LivingHexagon] {
  
  protected def attributes: Seq[VertexAttribute] = {
    Seq(VertexAttribute.Position(),
      new VertexAttribute(Usage.Generic, 2, "a_center"),
      new VertexAttribute(Usage.Generic, 3, "a_barycentric"),
      new VertexAttribute(Usage.Generic, 1, "a_tier"),
      new VertexAttribute(Usage.ColorPacked, 4, "a_color0"),
      new VertexAttribute(Usage.ColorPacked, 4, "a_color1"),
      new VertexAttribute(Usage.Generic, 1, "a_birthtime"),
      new VertexAttribute(Usage.Generic, 1, "a_deathtime"))
  }

  def hexagonWidth = LivingHexagon.scaling
}