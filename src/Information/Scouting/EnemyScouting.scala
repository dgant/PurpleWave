package Information.Scouting

import Lifecycle.With
import Performance.Cache
import Utilities.UnitFilters.IsWorker
import ProxyBwapi.Races.Zerg

trait EnemyScouting {
  def enemyHasScoutedUsWithWorker: Boolean = _enemyHasScoutedUsWithWorker
  val zonesToLookForEnemyScouts = new Cache(() => With.geography.ourMetro.zones)
  val enemyScouts = new Cache(() => With.units.enemy.filter(u => u.isAny(Zerg.Overlord, IsWorker) && u.likelyStillThere && zonesToLookForEnemyScouts().contains(u.zone)))

  private var _enemyHasScoutedUsWithWorker = false

  protected def updateEnemyScouting(): Unit = {
    _enemyHasScoutedUsWithWorker ||= With.geography.ourBases.exists(_.enemies.exists(IsWorker))
  }
}
