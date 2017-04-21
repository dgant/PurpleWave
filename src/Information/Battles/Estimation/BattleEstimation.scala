package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.Estimation.BattleEstimator.{DamageMap, Participant}
import Information.Battles.TacticsTypes.TacticsOptions

class BattleEstimation(
  val battle              : Battle,
  val tactics             : TacticsOptions,
  val damageToUs          : Double,
  val damageToEnemy       : Double,
  val participantsUs      : Vector[Participant],
  val participantsEnemy   : Vector[Participant],
  val damageDealtByUs     : DamageMap,
  val damageDealtByEnemy  : DamageMap)
