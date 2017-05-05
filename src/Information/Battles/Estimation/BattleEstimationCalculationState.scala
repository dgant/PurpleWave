package Information.Battles.Estimation

import Information.Battles.TacticsTypes.TacticsOptions

case class BattleEstimationCalculationState (
  val avatar              : BattleEstimationUnit,
  val tactics             : TacticsOptions,
  var x                   : Double,
  var spread              : Double,
  var damage              : Double = 0.0,
  var participationGround : Double = 1.0,
  var participationAir    : Double = 1.0)
