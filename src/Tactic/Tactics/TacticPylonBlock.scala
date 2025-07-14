package Tactic.Tactics

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsWorker
import Utilities.UnitPreferences.PreferClose

class TacticPylonBlock extends Tactic {

  lock.setMatcher(IsWorker).setCounter(CountOne)

  var bases     : Vector[Base]             = Vector.empty
  var lastDeath : Int                      = 0
  var lastWorker: Option[FriendlyUnitInfo] = None

  override def launch(): Unit = {
    if (lastWorker.exists( ! _.alive)) {
      lastDeath = With.frame
    }
    lastWorker = None

    var abort = false
    abort ||= With.self.isZerg
    abort ||= With.enemies.exists(_.isZerg)
    abort ||= With.framesSince(lastDeath) < Minutes(1)()
    abort ||= With.units.countOurs(IsWorker) < 38
    abort ||= With.scouting.enemyProximity > 0.5 && units.isEmpty
    if (abort) return

    bases = With.geography.preferredExpansionsEnemy
    bases = bases.filter(base => With.scouting.weControl(base.townHallTile))
    bases = bases.filter(_.owner.isNeutral)
    bases = bases.filter(_.mineralsLeft > 7500)
    bases = bases.filter(base => With.grids.scoutingPathsStartLocations.get(base.townHallTile.add(1, 1)) > 15)
    bases = bases.filterNot(_.island)
    bases = bases.filterNot(_.naturalOf.exists(_.isEnemy))
    bases = bases.filterNot(With.geography.preferredExpansionsOurs.take(3).contains)
    bases = bases.filterNot(_.enemies.exists(_.likelyStillThere))
    bases = bases.filterNot(base => base.units.exists(_.friendly.exists(u => u.unitClass.isBuilding && u.tileArea.intersects(base.townHallArea))))

    bases.headOption.foreach(base => {
      val tile = base.townHallTile.add(1, 1)
      lastWorker = lock.setPreference(PreferClose(tile.center)).acquire().headOption
      lock.units.foreach(_.intend(this)
        .setCanSneak(true)
        .setTerminus(tile.center)
        .setScout(Seq(tile)))
    })
  }

  override def toString: String = super.toString + ?(units.isEmpty, "", f" ${bases.mkString(" -> ")}")
}
