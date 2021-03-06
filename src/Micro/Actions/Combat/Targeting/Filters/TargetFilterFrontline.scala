package Micro.Actions.Combat.Targeting.Filters
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Target
import Planning.UnitMatchers.MatchTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFrontline extends TargetFilter {
  // Prefer targeting front-to-back
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    actor.pixelDistanceEdge(target) <= actor.pixelRangeAgainst(target) + diveDistance(actor, target)
  }

  def diveDistance(actor: FriendlyUnitInfo, target: UnitInfo): Double = {
    val freedomPixelsSafety = -actor.matchups.pixelsOfEntanglement
    val freedomPixelsTime   = Math.max(actor.cooldownLeft * actor.topSpeed, actor.matchups.pixelsToReachAnyTarget)
    val injuryPixels        = 32 * Target.injury(target)
    val valuePixels         = if (target.isAny(MatchTank, Protoss.Carrier, Protoss.Reaver, Zerg.Lurker)) 32 else 0
    val healingPixels       = if (target.beingHealed) -target.unitClass.dimensionMax else 0
    val bonusPixels         = injuryPixels + valuePixels + healingPixels
    val kitingPixels        = if (actor.inRangeToAttack(target) || actor.topSpeed >= target.topSpeed || actor.pixelRangeAgainst(target) >= target.effectiveRangePixels) 0 else -32
    PurpleMath.clamp(bonusPixels, freedomPixelsSafety, freedomPixelsTime)
  }
}
