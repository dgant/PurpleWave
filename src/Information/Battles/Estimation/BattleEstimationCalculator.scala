package Information.Battles.Estimation

import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}

abstract class BattleEstimationCalculator {
  
  val avatarUs      : BattleEstimationUnit
  val avatarEnemy   : BattleEstimationUnit
  val tacticsUs     : TacticsOptions
  val tacticsEnemy  : TacticsOptions
  
  var result = new BattleEstimationResult
  
  def recalculate() {
    result = new BattleEstimationResult
    if (avatarUs.totalUnits == 0 || avatarEnemy.totalUnits == 0) return
    
    val frames = 24
      (if (tacticsUs    .has(Tactics.Movement.Flee))  1 else 4) *
      (if (tacticsEnemy .has(Tactics.Movement.Flee))  1 else 4)
    
    //TODO: Account for damage dropoff due to small units dying
    //TODO: Incorporate more tactics
    result.costToEnemy  = avatarEnemy.subjectiveValue / avatarEnemy.totalHealth * dealDamage(frames, avatarUs,    avatarEnemy,  tacticsUs)
    result.costToUs     = avatarEnemy.subjectiveValue / avatarEnemy.totalHealth * dealDamage(frames, avatarEnemy, avatarUs,     tacticsEnemy)
  }
  
  private def dealDamage(
    frames  : Int,
    from    : BattleEstimationUnit,
    to      : BattleEstimationUnit,
    tactics : TacticsOptions):Double = {
    val airFocus = if (tactics.has(Tactics.Focus.Ground)) 0.0 else if (tactics.has(Tactics.Focus.Air)) 1.0 else to.totalFlyers / to.totalUnits
    val groundFocus = 1.0 - airFocus
    val perFrame =
      to.damageScaleGroundConcussive  * from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus +
      to.damageScaleGroundExplosive   * from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus +
      to.damageScaleGroundNormal      * from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus +
      to.damageScaleAirConcussive     * from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus +
      to.damageScaleAirExplosive      * from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus +
      to.damageScaleAirNormal         * from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus
    perFrame * frames
  }
}
