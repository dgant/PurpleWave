package Tactic.Tactics

import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.immutable.HashMap

final class TacticExpandByLiftoff extends Tactic {

  override def launch(): Unit = {
    val minedOutCCs = With.units.ours.filter(Terran.CommandCenter).filter(cc => cc.flying || cc.base.exists(b => b.mineralsLeft == 0 && (b.gasLeft == 0 || With.geography.ourMiningBases.isEmpty))).toVector
    val goals       = With.geography.preferredExpansionsOurs.take(minedOutCCs.length).map(_.townHallTile)
    var mapping     = new HashMap[UnitInfo, Tile]()
    goals.foreach(goal => mapping += ((minedOutCCs.filterNot(mapping.contains).minBy(_.tileTopLeft.tileDistanceSquared(goal)), goal)))

    if (mapping.isEmpty) return

    lock.setMatcher(mapping.keySet.contains).acquire().foreach(cc => {
      cc.intend(this)
      if (cc.flying) {
        cc.intent.setLand(mapping(cc))
      } else {
        cc.intent.setShouldLiftoff(true)
      }
    })
  }
}
