package Planning.Composition.UnitCounters

import Information.Battles.Prediction.Estimation.{AvatarBuilder, EstimateAvatar}
import Information.Battles.Prediction.Prediction
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitCountCombat(
  val enemies       : Iterable[UnitInfo],
  val alwaysAccept  : Boolean = false,
  val overkill      : Double = 2.0)
    extends UnitCounter {
  
  val builder = new AvatarBuilder
  var lastEstimation: Prediction = _
  enemies.foreach(builder.addUnit)
  
  /////////////////
  // UnitCounter //
  /////////////////
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = EstimateAvatar.calculate(builder)
    
    // Avatar
    units.isEmpty || ! overkills
  }
  
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.foreach(builder.addUnit)
    lastEstimation = EstimateAvatar.calculate(builder)
  
    alwaysAccept || sufficient
  }
  
  private def overkills   : Boolean = lastEstimation.costToUs  < lastEstimation.costToEnemy / overkill
  private def sufficient  : Boolean = lastEstimation.costToUs  < lastEstimation.costToEnemy
}
