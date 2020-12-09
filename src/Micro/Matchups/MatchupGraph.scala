package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.collection.mutable

class MatchupGraph {

  val entrants = new mutable.HashMap[BattleLocal, mutable.ArrayBuffer[UnitInfo]]
  
  def run() {
    assignEntrants()
    With.units.all.foreach(u => u.matchups = MatchupAnalysis(u))
  }
  
  private def assignEntrants() {
    // Battle clustering is slow and can take 1-2 seconds.
    // Sometimes we need to include units faster than that.
    // For example: Spider mines that just popped up, or Siege Tanks/Lurkers that just started shooting at us unexpectedly.
    entrants.clear
    With.units.playerOwned.foreach(entrant => {
      if (entrant.alive
        &&  entrant.battle.isEmpty
        &&  With.framesSince(entrant.frameDiscovered) < 72
        && BattleClassificationFilters.isEligibleLocal(entrant)) {
          val battle = ByOption.minBy(With.units.all.view.filter(_.battle.isDefined))(_.pixelDistanceSquared(entrant)).flatMap(_.battle)
          if (battle.isDefined) {
            if ( ! entrants.contains(battle.get)) {
              entrants.put(battle.get, new mutable.ArrayBuffer[UnitInfo])
            }
            entrants(battle.get) += entrant
          }
        }
      })
  }
}
