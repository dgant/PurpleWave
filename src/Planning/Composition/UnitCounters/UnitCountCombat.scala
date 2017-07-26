package Planning.Composition.UnitCounters

import Information.Battles.Estimations.{AvatarBuilder, Estimation, Estimator}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitCountCombat(
  val enemies       : Iterable[UnitInfo],
  val alwaysAccept  : Boolean,
  val overkill      : Double = 2.0) extends UnitCounter {
  
  val builder = new AvatarBuilder
  var lastEstimation: Estimation = _
  enemies.foreach(builder.addUnit)
  
  /////////////////
  // UnitCounter //
  /////////////////
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = Estimator.calculate(builder)
    ! isSufficient
  }
  
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = Estimator.calculate(builder)
    isAcceptable
  }
  
  //////////
  // Guts //
  //////////
  
  private def isSufficient: Boolean = {
    (builder.avatarEnemy.totalUnits == 0 || (lastEstimation.enemyDies && ! lastEstimation.weSurvive)) &&
    lastEstimation.costToEnemy * overkill >= lastEstimation.costToUs
  }
  
  private def isAcceptable: Boolean = {
    alwaysAccept || isSufficient || lastEstimation.weGainValue || lastEstimation.enemyDies
  }
}
