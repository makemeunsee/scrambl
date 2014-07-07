package hexatext.mvc.model

import world2d.LivingHexagon
import models.FlakeGenerator

class FlakeExplorerModel extends FlakeModel {

  private var flakeBranchLength = FlakeGenerator.sizeMax/2

  override def flakeSize = flakeBranchLength

  override def resizeFlake(size: Int): Set[LivingHexagon] = {
    val properSize = math.min(FlakeGenerator.sizeMax+1, math.max(0, size))
    if (flakeSize != properSize) {
      flakeBranchLength = properSize
      val newFlakeView: Set[LivingHexagon] =
        if (properSize > 1) flake.transpose.flatten.take((flakeSize-1) * 6).toSet + new LivingHexagon(0,0)
        else if (properSize > 0) Set(new LivingHexagon(0,0))
        else Set.empty
      val diff1 = toggle(flakeView -- newFlakeView)
      val diff2 = toggle(newFlakeView -- flakeView)
      flakeView = newFlakeView
      diff1 ++ diff2
    } else {
      Set.empty
    }
  }
}
