package Information.Battles.Estimation

import Information.Battles.TacticsTypes.TacticsOptions

case class BattleEstimationState(
  val avatar      : BattleEstimationUnit,
  val tactics     : TacticsOptions,
  var x           : Double,
  var pixelsAway  : Double) {
  
  var pixelsRegrouped             : Double = _
  var damageReceived              : Double = _
  var deaths                      : Double = _
  var arrivedGroundShooters       : Double = _
  var arrivedAirShooters          : Double = _
}