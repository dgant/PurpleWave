package Planning.Composition.UnitCounters

import Information.Battles.Estimation.{AvatarBuilder, Estimation, Estimator}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitCountCombat(
  enemies: Iterable[UnitInfo],
  alwaysAccept: Boolean) extends UnitCounter {
  
  val builder = new AvatarBuilder
  var lastEstimation: Estimation = _
  
  /////////////////
  // UnitCounter //
  /////////////////
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = Estimator.calculate(builder)
    isSufficient
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
    lastEstimation.weGainValue || lastEstimation.enemyDies
  }
  
  private def isAcceptable: Boolean = {
    alwaysAccept || isSufficient || lastEstimation.weGainValue || lastEstimation.enemyDies
  }
}
