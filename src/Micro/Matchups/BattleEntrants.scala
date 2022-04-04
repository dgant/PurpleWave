package Micro.Matchups

import Information.Battles.BattleFilters
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Maff
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEntrants extends TimedTask {

  val entrants = new mutable.HashMap[Battle, mutable.ArrayBuffer[UnitInfo]]
  
  override protected def onRun(budgetMs: Long): Unit = {
    assignEntrants()
  }
  
  private def assignEntrants() {
    // Battle clustering is slow and can take 1-2 seconds.
    // Sometimes we need to include units faster than that.
    // For example: Spider mines that just popped up, or Siege Tanks/Lurkers that just started shooting at us unexpectedly.
    entrants.clear
    With.units.playerOwned.foreach(entrant => {
      if (entrant.alive
        && entrant.battle.isEmpty
        && With.framesSince(entrant.frameDiscovered) < 72
        && BattleFilters.local(entrant)) {
          val battle = Maff.minBy(With.units.all.view.filter(_.battle.isDefined))(_.pixelDistanceSquared(entrant)).flatMap(_.battle)
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
