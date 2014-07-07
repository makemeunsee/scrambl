package rendering.shaders

import scala.util.Random
import world2d.LivingHexagon
import rendering.GenericColor
import rendering.Color

trait LavaBasalt {
  val colderLavaBaseColor = 0xd83f00ff
  val hotterLavaBaseColor = 0xf24c00ff
  val basaltColor         = 0x201008FF
  val noiseScaling = 0.16f
}

class BackgroundShader(val color0: GenericColor,
                       val color1: GenericColor,
                       override val border: BorderMode = NoFX,
                       override val highlighting: HighlightMode = NoFX,
                       override val cubic: Boolean = false)
extends ShaderModule[LivingHexagon] {
  override def blendingRate = 1f
  override def sprouting = NoFX
  val defaultBirth: LivingHexagon => Float = _ => Random.nextInt(1000)
  val defaultDeath: LivingHexagon => Float = _ => Float.MaxValue
  def hexagonWidth = LivingHexagon.scaling
}

class BlockShader(color: GenericColor,
                  override val border: BorderMode = NoFX,
                  override val sprouting: SproutMode = NoFX,
                  override val highlighting: HighlightMode = NoFX,
                  override val cubic: Boolean = false,
                  val defaultBirth: LivingHexagon => Float = _ => 0,
                  val defaultDeath: LivingHexagon => Float = _ => 0)
extends ShaderModule[LivingHexagon] {
  val color0 = color
  val color1 = color
  override val blendingRate = 1f
  def hexagonWidth = LivingHexagon.scaling
}

case class ShadersPack(implicit intToColor: Int => Color) {
  
  sealed abstract class ShaderPair {
    def backgroundShader: BackgroundShader
    def blockShader: BlockShader
  }

  val Artsy = new ShaderPair {
    val backgroundShader = new BackgroundShader(
      GenericColor(0xc41a4fff,0.04f,0.04f,(20.0f,20.0f,0.3f)),
      GenericColor(0x620d27ff,0.04f,0.04f,(20.0f,0.7f,10f)),
      cubic = true)

    val blockCol = GenericColor(0x1f1f1fff,0.14f,0.14f,(0.4f,0.4f,0.4f))
    val blockShader = new BlockShader(blockCol, Border(0xffffff22, 2f),Shrinking, highlighting = NoFX)
  }

  val Electric = new ShaderPair {
    val backgroundShader = new BackgroundShader(
      GenericColor(0x2236afff,0.14f,0.14f,(0.33f,0.5f,40f)),
      GenericColor(0x251f44ff,0.14f,0.14f,(0.33f,0.5f,40f)),
      cubic = true)

    val blockCol = GenericColor(0x663333ff,0.14f,0.14f,(0f,0f,0.5f))
    val blockShader = new BlockShader(blockCol, Border(0x88884488, 1.2f),Shrinking, highlighting = NoFX,cubic = true)
  }

  val BlueOnBlue = new ShaderPair {
    val blockCol = GenericColor(0x395ce1ff,0.32f,0.32f,(0.5f,0.5f,0.5f))

    val blockShader = new BlockShader(blockCol, sprouting = Fading)

    val backgroundShader = new BackgroundShader(GenericColor(0x2935caff,0.01f,0.01f,(0.33f,0.5f,1.0f)),
      GenericColor(0x141a65ff,0.01f,0.01f,(0.33f,0.5f,1.0f)))
  }

  val MetallicViolet = new ShaderPair {
    val blockCol = GenericColor(0x330954ff,0.16f,0.16f,(0.5f,0.5f,0.5f))

    val blockShader = new BlockShader(blockCol, sprouting = Fading, highlighting = NoFX)

    val backgroundShader = new BackgroundShader(GenericColor(0xdd92c7ff,0.02f,0.02f,(0.33f,0.5f,1.0f)),
      GenericColor(0x6e4963ff,0.02f,0.02f,(0.33f,0.5f,1.0f)),
      border = Border(0x6b9a222e,1.6f))
  }

  val LaitFraise = new ShaderPair {

    val blockCol = GenericColor(0xfb735dff,0.02f,0.02f,(0.5f,0.7f,0.6f),425,185)

    val blockShader = new BlockShader(blockCol,sprouting = Shrinking,cubic = true)

    val backgroundShader = new BackgroundShader(
      GenericColor(0xf0adadff,0.01f,0.01f,(0.8f,0.9f,0.5f),434,346),
      GenericColor(0x785656ff,0.01f,0.01f,(7.0f,4.0f,3.0f),28,69),
      Border(0xe5b75ba7,3.2f),cubic = true)
  }

  val GoldenCore = new ShaderPair {
    val blockCol = GenericColor(0xec4906ff,0.01f,0.01f,(0.6f,4.0f,10.0f),160,153)

    val blockShader = new BlockShader(blockCol,sprouting = Fading,highlighting = Pulsating(rate = 0.3f),cubic = true)

    val backgroundShader = new BackgroundShader(
      GenericColor(0xa722c4ff,0.32f,0.32f,(0.6f,2.0f,2.0f),215,199),
      GenericColor(0x531162ff,0.32f,0.32f,(0.3f,7.0f,7.0f),401,70),
      Border(0x2c0ccfe7,2.4f),cubic = true)
  }

  val LavaBasaltGradient = new ShaderPair with LavaBasalt {

    val gradCol1 = GenericColor(colderLavaBaseColor,
      noiseScaling, noiseScaling,
      (0.33f, 0.5f, 1f))
    val gradCol2 = GenericColor(hotterLavaBaseColor,
      noiseScaling, noiseScaling,
      (0.33f, 0.5f, 1f))

    val backgroundShader = new BackgroundShader(gradCol1, gradCol2)

    val gradCol = GenericColor(basaltColor,
      1f, 1f,
      (0.7f, 0.7f, 0.7f),
      shadingCoeffs = (0.1f, 0.1f, 0.1f))

    val blockShader = new BlockShader(gradCol, sprouting = Shrinking, highlighting = NoFX, border = Border(0))
  }

  val LavaBasaltCubic = new ShaderPair with LavaBasalt {

    val cubeCol1 = GenericColor(colderLavaBaseColor,
      noiseScaling, noiseScaling,
      (0.33f, 0.5f, 0.5f),
      128, 0,
      shadingCoeffs = (-0.1f, -0.1f, -0.1f))
    val cubeCol2 = GenericColor(hotterLavaBaseColor,
      noiseScaling, noiseScaling,
      (0.33f, 0.5f, 0.5f),
      504, 0,
      shadingCoeffs = (-0.1f, -0.1f, -0.1f))

    val backgroundShader = new BackgroundShader(cubeCol1, cubeCol2, cubic = true)

    val cubeCol = GenericColor(basaltColor,
      1, 1,
      (0.5f, 0.5f, 0.5f),
       shadingCoeffs = (0.1f, 0.1f, 0.1f))

    val blockShader = new BlockShader(cubeCol, sprouting = Fading, cubic = true)
  }

  val Kurosawa = new ShaderPair {
    val blockCol = GenericColor(0xba842fff,0.01f,0.01f,(0.3f,4.0f,8.0f), shadingCoeffs = (0.0f,0.0f,0.0f))

    val blockShader = new BlockShader(
      blockCol, Border(0xaf07c69a,1.6f),sprouting = Fading)

    val backgroundShader = new BackgroundShader(
      GenericColor(0x7673f3ff,0.16f,0.16f,(50.0f,0.2f,10.0f), shadingCoeffs = (0.0f,0.0f,0.0f)),
      GenericColor(0x3b3979ff,0.16f,0.16f,(60.0f,8.0f,20.0f), shadingCoeffs = (0.0f,0.0f,0.0f)),
      Border(0xd513df5b,1.6f))
  }

  val Mosaic = new ShaderPair {
    val blockCol = GenericColor(0x63b3fbff,0.01f,0.01f,(2.0f,0.5f,4.0f))

    val blockShader = new BlockShader(blockCol,sprouting = Fading, highlighting = NoFX,cubic = true)

    val backgroundShader = new BackgroundShader(
      GenericColor(0xc4bf2aff,1.28f,1.28f,(3.0f,9.0f,60.0f)),
      GenericColor(0x625f15ff,1.28f,1.28f,(7.0f,0.1f,5.0f)),
      Border(0x2541e137,2.4f))
  }
  
  val PurpleInRain = new ShaderPair {
    val blockCol = GenericColor(0xfd31f3ff,0.01f,0.01f,(0.7f,3.0f,0.2f))
      
    val blockShader = new BlockShader(blockCol, highlighting = NoFX,sprouting = Shrinking)
    
    val backgroundShader = new BackgroundShader(
        GenericColor(0xa269d8ff,0.32f,0.16f,(0.9f,10.0f,50.0f)),
        GenericColor(0x51346cff,0.02f,0.64f,(60.0f,6.0f,0.7f)),
        Border(0x49447a56,3.2f))
  }

  val Sunset = new ShaderPair {
    val blockCol = GenericColor(0x322005ff,0.02f,0.02f,(20.0f,4.0f,0.1f), 19, 466)
    val blockShader = new BlockShader(blockCol, highlighting = NoFX,sprouting = Fading)

    val backgroundShader = new BackgroundShader(
      GenericColor(0xf87408ff,0.08f,0.01f,(0.6f,0.7f,0.8f)),
      GenericColor(0xf87408ff,0.08f,0.01f,(0.6f,0.7f,0.8f)))
  }

  val Sunset2 = new ShaderPair {
    val blockCol = GenericColor(0x0c0c00ff,0.02f,0.02f,(20.0f,4.0f,0.1f), 19, 466)
    val blockShader = new BlockShader(blockCol,sprouting = Fading, highlighting = Pulsating())

    val backgroundShader = new BackgroundShader(
      GenericColor(0xf87408ff,0.08f,0.01f,(0.6f,0.7f,0.8f)),
      GenericColor(0xf87408ff,0.08f,0.01f,(0.6f,0.7f,0.8f)))
  }

  val Impressionist = new ShaderPair {

    val blockCol = GenericColor(0xfd31f3ff,0.01f,0.01f,(0.7f,3.0f,0.2f))
    val blockShader = new BlockShader(blockCol,sprouting = Shrinking)

    val backgroundShader = new BackgroundShader(
      GenericColor(0xc317a0ff,0.32f,0.16f,(6.0f,70.0f,0.8f)),
      GenericColor(0x610b50ff,0.16f,0.01f,(2.0f,60.0f,90.0f)),
      Border(0x2abd5605,1.6f),highlighting = Pulsating())
  }
  
  val StroboAmbers = new ShaderPair {
    val blockShader = new BlockShader(
        GenericColor(0xa4483fff,0.08f,0.08f,(0.5f,0.3f,0.8f), shadingCoeffs = (-0.1f,0.1f,-0.1f)),
        Border(0x00000005,0.8f),
        sprouting = Fading, highlighting = NoFX, cubic = false)

    val backgroundShader = new BackgroundShader(
        GenericColor(0xca1302ff,0.04f,0.32f,(5.0f,9.0f,0.1f)),
        GenericColor(0x650901ff,1.28f,0.32f,(100.0f,9.0f,100.0f)),
        Border(0x7a71533a,1.6f), highlighting = Pulsating(1.0f,0.4f,-1.5f))
  }
  
  val GreenSlither = new ShaderPair {
    val blockShader = new BlockShader(
        GenericColor(0xe1699bff,0.08f,1.28f,(0.7f,4.0f,1.0f)),
        sprouting = Fading, highlighting = NoFX, cubic = true)
    
    val backgroundShader = new BackgroundShader(
        GenericColor(0x75f786ff,0.08f,0.02f,(5.0f,0.5f,100f)),
        GenericColor(0x3a7b43ff,0.01f,0.02f,(4.0f,4.0f,8.0f)),
        Border(0xc55c9938,0.8f), highlighting = Pulsating(0.2f,0.5f,0.6f))
  }
  
  val values = Seq(Artsy, Electric, BlueOnBlue, MetallicViolet, LaitFraise, GoldenCore,
                   LavaBasaltGradient, LavaBasaltCubic, Kurosawa, Mosaic, PurpleInRain,
                   Sunset, Sunset2, Impressionist, StroboAmbers, GreenSlither)
}