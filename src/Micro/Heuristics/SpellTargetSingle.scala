package Micro.Heuristics

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object SpellTargetSingle {
  
  def chooseTarget(
    caster              : FriendlyUnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo, FriendlyUnitInfo) => Double): Option[UnitInfo] = {
    
    val targets = SpellTargets(caster)
      .filter(_.visible)
      .filter(_.pixelDistanceCenter(caster) <= searchRadiusPixels)
      .filterNot(caster==)
      .filterNot(_.effectivelyCloaked)
    
    if (targets.isEmpty) return None

    targets.foreach(t => t.spellTargetValue = evaluate(t, caster))

    Some(targets.maxBy(_.spellTargetValue)).filter(_.spellTargetValue >= minimumValue)
  }
}
