package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Circle
import Planning.MacroFacts
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountEverything

class ChillOverlords extends Tactic {
  
  val overlords = new LockUnits(this, Zerg.Overlord, CountEverything)

  def launch(): Unit = {
    if ( ! With.self.isZerg) return
    if (With.self.hasUpgrade(Zerg.OverlordSpeed)) return
    if (MacroFacts.enemyShownCloakedThreat) return

    overlords.acquire().foreach(chillOut(_, overlords.units.size))
  }
  
  private def chillOut(overlord: FriendlyUnitInfo, count: Int): Unit = {
    val base = Maff.minBy(With.geography.ourBases.map(_.heart.center))(overlord.pixelDistanceSquared)
    val tile = base.map(b => Maff.sample(Circle(Math.sqrt(count).toInt).map(b.tile.add))).getOrElse(With.geography.home)
    overlord.intend(this).setTerminus(tile.center)
  }
}
