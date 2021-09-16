package Information

import Lifecycle.With
import Performance.Cache
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Zerg

trait EnemyScouting {
  def enemyHasScoutedUsWithWorker: Boolean = _enemyHasScoutedUsWithWorker
  val basesToLookForEnemyScouts = new Cache(() => (With.geography.ourBases :+ With.geography.ourNatural).distinct)
  val enemyScouts = new Cache(() => With.units.enemy.filter(u => u.isAny(Zerg.Overlord, MatchWorker) && u.likelyStillThere && u.base.exists(basesToLookForEnemyScouts().contains)))

  private var _enemyHasScoutedUsWithWorker = false

  protected def updateEnemyScouting(): Unit = {
    _enemyHasScoutedUsWithWorker = _enemyHasScoutedUsWithWorker || With.geography.ourBases.exists(_.units.exists(u => u.isEnemy && MatchWorker(u)))
  }
}
