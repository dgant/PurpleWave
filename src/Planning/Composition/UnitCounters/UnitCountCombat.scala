package Planning.Composition.UnitCounters

import Information.Battles.Estimations.{AvatarBuilder, Estimation, Estimator}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitCountCombat(
  val enemies: Iterable[UnitInfo],
  val alwaysAccept: Boolean) extends UnitCounter {
  
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
    lastEstimation.netCost >= 0.0
  }
  
  private def isAcceptable: Boolean = {
    alwaysAccept || isSufficient || lastEstimation.weGainValue || lastEstimation.enemyDies
  }
}
