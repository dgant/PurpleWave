package Information

import Lifecycle.With
import Performance.Cache
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Zerg

trait EnemyScouting {
  def enemyHasScoutedUsWithWorker: Boolean = _enemyHasScoutedUsWithWorker
  val zonesToLookForEnemyScouts = new Cache(() => With.geography.home.metro.map(_.zones).getOrElse(Vector(With.geography.home.zone, With.geography.ourNatural.zone)))
  val enemyScouts = new Cache(() => With.units.enemy.filter(u => u.isAny(Zerg.Overlord, MatchWorker) && u.likelyStillThere && zonesToLookForEnemyScouts().contains(u.zone)))

  private var _enemyHasScoutedUsWithWorker = false

  protected def updateEnemyScouting(): Unit = {
    _enemyHasScoutedUsWithWorker = _enemyHasScoutedUsWithWorker || With.geography.ourBases.exists(_.units.exists(u => u.isEnemy && MatchWorker(u)))
  }
}
