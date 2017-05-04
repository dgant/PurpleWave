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
    
    val frameStep = 24
    val frames = frameStep *
      (if (tacticsUs    .has(Tactics.Movement.Flee))  1 else 4) *
      (if (tacticsEnemy .has(Tactics.Movement.Flee))  1 else 4)
  
    // Calculate combat in batches
    // Simulate drop in damage output as units die
    val meanHealthUs        = avatarUs.totalHealth    / avatarUs.totalUnits
    val meanHealthEnemy     = avatarEnemy.totalHealth / avatarEnemy.totalUnits
    
    var damageToUs          = 0.0
    var damageToEnemy       = 0.0
    var startingHealthUs    = avatarUs.totalHealth
    var startingHealthEnemy = avatarUs.totalHealth
    
    // Account for dropoff as units die
    // Two levers affect how this works:
    // * how we calculate # of living units. The current calculation assumes that all damage is focus fired (an overestimate of dropoff)
    // * frameStep, which when larger reduces the impact of dropoff. Let's choose a big one (to balance the above with an underestimate of dropoff)
    (0 to frames by frameStep).foreach(frame => {
      val livingUnitsUs     = Math.max(0.0, Math.ceil((startingHealthUs     - damageToUs)     / startingHealthUs))
      val livingUnitsEnemy  = Math.max(0.0, Math.ceil((startingHealthEnemy  - damageToEnemy)  / startingHealthEnemy))
      if (livingUnitsUs > 0 && livingUnitsEnemy > 0) {
        damageToUs    += dealDamage(frameStep, avatarEnemy, avatarUs,    tacticsEnemy)  * livingUnitsEnemy  / avatarEnemy.totalUnits
        damageToEnemy += dealDamage(frameStep, avatarUs,    avatarEnemy, tacticsUs)     * livingUnitsUs     / avatarUs.totalUnits
      }
    })
    
    result.costToEnemy  = totalCost(frames, damageToEnemy, avatarEnemy)
    result.costToUs     = totalCost(frames, damageToUs, avatarUs)
  }
  
  private def totalCost(frames: Int, damage: Double, avatar: BattleEstimationUnit) = {
    avatar.subjectiveValueCostPerFrame * frames +
      avatar.subjectiveValue * Math.max(0.0, Math.min(1.0, damage / avatar.totalHealth))
  }
  
  private def dealDamage(
    frames      : Int,
    from        : BattleEstimationUnit,
    to          : BattleEstimationUnit,
    tacticsFrom : TacticsOptions)
      : Double = {
    val airFocus = if (tacticsFrom.has(Tactics.Focus.Ground)) 0.0 else if (tacticsFrom.has(Tactics.Focus.Air)) 1.0 else to.totalFlyers / to.totalUnits
    val groundFocus = 1.0 - airFocus
    val perFrame =
      to.damageScaleGroundConcussive  * from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus +
      to.damageScaleGroundExplosive   * from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus +
      to.damageScaleGroundNormal      * from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus +
      to.damageScaleAirConcussive     * from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus +
      to.damageScaleAirExplosive      * from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus +
      to.damageScaleAirNormal         * from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus
    perFrame * frames / to.totalUnits
  }
}
