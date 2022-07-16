package Information.Scouting

import Lifecycle.With
import ProxyBwapi.Buildable
import Utilities.CountMap
import Utilities.Time.Forever

trait Debuts {
  private val ourDebuts   = new CountMap[Buildable](Forever())
  private val enemyDebuts = new CountMap[Buildable](Forever())

  protected def updateDebuts(): Unit = {
    With.units.ours .filter(_.visibleToOpponents) .foreach(u => ourDebuts   .reduceTo(u.unitClass, With.frame + u.remainingCompletionFrames))
    With.units.enemy.filter(_.visible)            .foreach(u => enemyDebuts .reduceTo(u.unitClass, With.frame + u.remainingCompletionFrames))
  }

  def ourDebut(buildables: Buildable*)    : Int = buildables.map(ourDebuts).min
  def enemyDebut(buildables: Buildable*)  : Int = buildables.map(enemyDebuts).min
}
