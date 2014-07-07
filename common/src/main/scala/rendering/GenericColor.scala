package rendering

import scala.util.Random
import world2d.Hexagon
import noise.SimplexNoise

case class GenericColor(
  baseColor: Color,
  noiseScalingX: Float,
  noiseScalingY: Float,
  noiseCoeffs: (Float, Float, Float),
  offsetX: Float = Random.nextInt(500),
  offsetY: Float = Random.nextInt(500),
  shadingCoeffs: (Float, Float, Float) = (-0.1f, -0.1f, -0.1f)) {

  def reseed: GenericColor = copy(offsetX = Random.nextInt(500), offsetY = Random.nextInt(500))

  def noise(h: Hexagon) = SimplexNoise.noise(h.x*noiseScalingX + offsetX, h.y*noiseScalingY + offsetY).toFloat

  def noisify(color: Color, n: Float): Color = {
    val (rc, gc, bc) = noiseCoeffs
    color
    .mul(1f + n * rc,
         1f + n * gc,
         1f + n * bc,
         1f)
  }

  def noised(h: Hexagon): Color = noisify(baseColor, noise(h))

  def shaded(h: Hexagon): Color = noised(h).add(shadingCoeffs._1, shadingCoeffs._2, shadingCoeffs._3, 0)
}

