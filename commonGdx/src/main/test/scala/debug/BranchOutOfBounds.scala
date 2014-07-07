import models.roaming.RoamingModel

object BranchOutOfBounds {

  def main(args: Array[String]) {
    val model = new RoamingModel(6781657234086997804l)
    model.reset()
    println(model.level)
    println(model.refBranch.branchLength)
    println(model.refBranch.playedLength)
    while (model.hasNextLevel) {
      model.refBranch.autoComplete()
      model.nextLevel()
      println(model.refBranch.branchLength)
      println(model.refBranch.playedLength)
    }
  }

}
