package Information.Battles.Prediction.Estimation

import Information.Battles.Prediction.Prediction
import Lifecycle.With

object EstimateAvatar {
  
  def calculate(avatarBuilder: AvatarBuilder): Prediction = {
    
    val output = new Prediction
    
    output.avatarUs         = avatarBuilder.avatarUs
    output.avatarEnemy      = avatarBuilder.avatarEnemy
    output.totalUnitsUs     = output.avatarUs.totalUnits
    output.totalUnitsEnemy  = output.avatarEnemy.totalUnits
    
    if (avatarBuilder.avatarUs.totalUnits <= 0 || avatarBuilder.avatarEnemy.totalUnits <= 0) return output
    
    val maxFrames =
      if(avatarBuilder.weRetreat || avatarBuilder.enemyRetreats)
        // Yeah, we need to do better than this.
        24 * 3
      else
        With.configuration.battleEstimationFrames
        
    val frameStep = 24
    while (output.frames < maxFrames && output.weSurvive && output.enemySurvives) {
      output.frames         += frameStep
      output.damageToUs     += dealDamage (avatarBuilder.avatarEnemy, avatarBuilder.avatarUs,     frameStep, output.deathsEnemy, output.damageToUs)
      output.damageToEnemy  += dealDamage (avatarBuilder.avatarUs,    avatarBuilder.avatarEnemy,  frameStep, output.deathsUs,    output.damageToEnemy)
      output.deathsUs       = deaths      (avatarBuilder.avatarUs,    output.damageToUs)
      output.deathsEnemy    = deaths      (avatarBuilder.avatarEnemy, output.damageToEnemy)
    }
    
    output.costToUs     = totalCost   (avatarBuilder.avatarUs,    output.damageToUs)
    output.costToEnemy  = totalCost   (avatarBuilder.avatarEnemy, output.damageToEnemy)
    
    output
  }
  
  private def dealDamage(from: Avatar, to: Avatar, frames: Double, fromDeaths: Double, damageExisting: Double): Double = {
    val damageOutput    = (from.totalUnits - fromDeaths) / from.totalUnits
    val airFocus        = to.totalFlyers / to.totalUnits
    val groundFocus     = 1.0 - airFocus
    
    val seconds = With.configuration.battleEstimationFrames / 24.0
    
    val damagePerFramePerUnit =
      to.vulnerabilityGroundConcussive  * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
      to.vulnerabilityGroundExplosive   * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
      to.vulnerabilityGroundNormal      * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
      to.vulnerabilityAirConcussive     * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
      to.vulnerabilityAirExplosive      * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
      to.vulnerabilityAirNormal         * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    Math.min(to.totalHealth - damageExisting, damagePerFramePerUnit * frames / to.totalUnits)
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
