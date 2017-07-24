package Information.Battles.Estimations

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.PurpleMath

object Estimator {
  
  def calculate(avatarBuilder: AvatarBuilder): Estimation = {
    
    val output = new Estimation
    
    output.avatarUs         = avatarBuilder.avatarUs
    output.avatarEnemy      = avatarBuilder.avatarEnemy
    output.totalUnitsUs     = output.avatarUs.totalUnits
    output.totalUnitsEnemy  = output.avatarEnemy.totalUnits
    
    if (avatarBuilder.avatarUs.totalUnits <= 0 || avatarBuilder.avatarEnemy.totalUnits <= 0) return output
    
    val frameStep = 24
    while (output.frames < With.configuration.battleEstimationFrames && output.weSurvive && output.enemySurvives) {
      output.frames         += frameStep
      output.damageToUs     = dealDamage  (avatarBuilder.avatarEnemy, avatarBuilder.avatarUs,     frameStep, output.deathsEnemy, output.damageToUs)
      output.damageToEnemy  = dealDamage  (avatarBuilder.avatarUs,    avatarBuilder.avatarEnemy,  frameStep, output.deathsUs,    output.damageToEnemy)
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
    
    val damagePerFrame =
      to.vulnerabilityGroundConcussive  * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
      to.vulnerabilityGroundExplosive   * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
      to.vulnerabilityGroundNormal      * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
      to.vulnerabilityAirConcussive     * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
      to.vulnerabilityAirExplosive      * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
      to.vulnerabilityAirNormal         * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    Math.min(to.totalHealth - damageExisting, damagePerFrame * frames)
  }
  
  def fromMatchups(battle: Battle): Estimation = {
  
    val output = new Estimation
    
    val us            = battle.us.units.map(_.matchups)
    val enemy         = battle.enemy.units.map(_.matchups)
    val lifetimeUs    = PurpleMath.nanToZero(us     .map(u => Math.min(u.framesToLiveDiffused, With.configuration.battleEstimationFrames)).sum / us.size)
    val lifetimeEnemy = PurpleMath.nanToZero(enemy  .map(u => Math.min(u.framesToLiveDiffused, With.configuration.battleEstimationFrames)).sum / enemy.size)
    val max           = Math.max(lifetimeUs, lifetimeEnemy)
    
    output.frames           = Math.min(lifetimeUs, lifetimeEnemy).toInt
    output.costToUs         = output.frames * us.map(_.vpfReceivingDiffused).sum
    output.costToEnemy      = output.frames * enemy.map(_.vpfReceivingDiffused).sum
    output.damageToUs       = output.frames * us.map(_.dpfReceivingDiffused).sum
    output.damageToUs       = output.frames * enemy.map(_.dpfReceivingDiffused).sum
    output.deathsUs         = us.count(_.framesToLiveDiffused < output.frames)
    output.deathsEnemy      = enemy.count(_.framesToLiveDiffused < output.frames)
    output.totalUnitsUs     = us.size
    output.totalUnitsEnemy  = enemy.size
    output
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
