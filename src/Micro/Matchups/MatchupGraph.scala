package Micro.Matchups

import Information.Battles.Types.BattleLocal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class MatchupGraph {
  
  private val maxUnitId = 10000 // Per jaj22, as usual
  
  val analyses  = new Array[MatchupAnalysis](maxUnitId)
  val entrants  = new mutable.HashMap[BattleLocal, mutable.HashSet[UnitInfo]]
  
  def get(unit: UnitInfo): MatchupAnalysis = {
    if (analyses(unit.id) == null) {
      analyses(unit.id) = new MatchupAnalysis(unit)
    }
    analyses(unit.id)
  }
  
  def run() {
    resetAnalyses()
    assignEntrants()
  }
  
  private def resetAnalyses() {
    var i = 0
    while (i < maxUnitId) {
      analyses(i) = null
      i += 1
    }
  }
  
  private def assignEntrants() {
    // Battle clustering is slow and can take 1-2 seconds.
    // Sometimes we need to include units faster than that.
    // For example: Spider mines that just popped up, or Siege Tanks/Lurkers that just started shooting at us unexpectedly.
    entrants.clear
    With.units.playerOwned.foreach(entrant => {
      if (entrant.alive
        && (entrant.complete || entrant.unitClass.isBuilding)
        &&  entrant.battle.isEmpty
        &&  With.framesSince(entrant.frameDiscovered) < 72) {
        val battle = assignToBattle(entrant)
        if (battle.isDefined) {
          if ( ! entrants.contains(battle.get)) {
            entrants.put(battle.get, new mutable.HashSet[UnitInfo])
          }
          entrants(battle.get) += entrant
        }
      }
    })
  }
  
  private def assignToBattle(unit: UnitInfo): Option[BattleLocal] = {
    val neighbors = With.units.inTileRadius(unit.tileIncludingCenter, 15).filter(_.battle.isDefined).groupBy(_.battle)
    if (neighbors.nonEmpty) {
      neighbors.maxBy(_._2.size)._1
    }
    else None
  }
  
}
