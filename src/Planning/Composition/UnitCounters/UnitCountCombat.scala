package Planning.Composition.UnitCounters

import Information.Battles.Estimations.{AvatarBuilder, Estimation, EstimateAvatar}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitCountCombat(
  val enemies       : Iterable[UnitInfo],
  val alwaysAccept  : Boolean = false,
  val overkill      : Double = 2.0)
    extends UnitCounter {
  
  val builder = new AvatarBuilder
  var lastEstimation: Estimation = _
  enemies.foreach(builder.addUnit)
  
  /////////////////
  // UnitCounter //
  /////////////////
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = EstimateAvatar.calculate(builder)
    
    units.isEmpty || sufficient
  }
  
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = EstimateAvatar.calculate(builder)
  
    alwaysAccept || sufficient
  }
  
  private def sufficient: Boolean = lastEstimation.netValue > 0
}
