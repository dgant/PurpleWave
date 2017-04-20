package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions

class BattleEstimation(
  val battle        : Battle,
  val tactics       : TacticsOptions,
  val damageToUs    : Double,
  val damageToEnemy : Double)
