package Micro.Heuristics.Spells

import ProxyBwapi.UnitInfo.UnitInfo

object TargetSingle {
  
  def chooseTarget(
    caster              : UnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo) => Double): Option[UnitInfo] = {
    
    val targets = caster.matchups.others.filter(_.pixelDistanceFast(caster) <= searchRadiusPixels)
    
    if (targets.isEmpty) {
      return None
    }
    
    val valueByTarget = targets.map(t => (t, evaluate(t))).toMap
    
    val best = valueByTarget.maxBy(_._2)
    
    if (best._2 >= minimumValue)
      Some(best._1)
    else
      None
  }
}
