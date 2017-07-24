package Information.Battles.Estimations

import Lifecycle.With

object Estimator {
  
  def calculate(avatarBuilder: AvatarBuilder): Estimation = {
    
    val output = new Estimation
    
    output.avatarUs       = avatarBuilder.avatarUs
    output.avatarEnemy    = avatarBuilder.avatarEnemy
    
    if (avatarBuilder.avatarUs.totalUnits <= 0 || avatarBuilder.avatarEnemy.totalUnits <= 0) return output
    
    while (output.frames < With.configuration.battleEstimationFrames && output.weSurvive && output.enemySurvives) {
      output.frames         += 24
      output.damageToUs     = dealDamage  (avatarBuilder.avatarEnemy, avatarBuilder.avatarUs,     output.deathsEnemy)
      output.damageToEnemy  = dealDamage  (avatarBuilder.avatarUs,    avatarBuilder.avatarEnemy,  output.deathsUs)
      output.deathsUs       = deaths      (avatarBuilder.avatarUs,    output.damageToUs)
      output.deathsEnemy    = deaths      (avatarBuilder.avatarEnemy, output.damageToEnemy)
    }
    
    output.costToUs     = totalCost   (avatarBuilder.avatarUs,    output.damageToUs)
    output.costToEnemy  = totalCost   (avatarBuilder.avatarEnemy, output.damageToEnemy)
    
    output
  }
  
  private def dealDamage(from: Avatar, to: Avatar, fromDeaths: Double): Double = {
    val damageOutput    = (from.totalUnits - fromDeaths) / from.totalUnits
    val airFocus        = to.totalFlyers / to.totalUnits
    val groundFocus     = 1.0 - airFocus
    val fromDenominator = from.totalUnits
    val toDenominator   = to.totalUnits
    
    val seconds = With.configuration.battleEstimationFrames / 24.0
    
    val damagePerFramePerUnit =
      to.vulnerabilityGroundConcussive  / toDenominator * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
      to.vulnerabilityGroundExplosive   / toDenominator * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
      to.vulnerabilityGroundNormal      / toDenominator * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
      to.vulnerabilityAirConcussive     / toDenominator * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
      to.vulnerabilityAirExplosive      / toDenominator * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
      to.vulnerabilityAirNormal         / toDenominator * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    Math.min(to.totalHealth, damagePerFramePerUnit * With.configuration.battleEstimationFrames / to.totalUnits)
  }
  
  // Examples:
  // 1 unit,   99 damage,  100 hp = 0 deaths
  // 2 units,  199 damage, 200 hp = 1 death
  // 2 units,  99 damage,  200 hp = 0 deaths
  //
  private def deaths(avatar: Avatar, damage: Double): Double = {
    Math.min(avatar.totalUnits, Math.floor(avatar.totalUnits * damage / avatar.totalHealth))
  }
  
  private def totalCost(avatar: Avatar, damage: Double) = {
    avatar.subjectiveValue * damage / avatar.totalHealth
  }
}
