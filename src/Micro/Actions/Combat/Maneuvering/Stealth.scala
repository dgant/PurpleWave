package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds

object Stealth extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.cloaked
    && unit.canMove
    && unit.intent.canFlee
    && ! With.yolo.active
    && ( ! unit.agent.shouldEngage || unit.presumptiveTarget.exists(_.base.exists(_.owner.isUs)))
    && ! unit.alliesBattle.exists(Protoss.Arbiter)
    && unit.matchups.enemies.exists(e => e.complete && ! e.unitClass.isWorker && e.attacksAgainst(unit) > 0))
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val impendingTurret = unit.matchups.enemies.exists(e =>
      Protoss.PhotonCannon(e)
      && unit.pixelDistanceEdge(e) + unit.topSpeed * e.remainingCompletionFrames < e.unitClass.sightPixels + 48
      && unit.pixelDistanceEdge(e) > Protoss.Probe.dimensionMax)
    lazy val nearDetectionRange = unit.tileArea.expand(2, 2).tiles.exists(With.grids.enemyDetection.inRange)
    lazy val nearMobileDetector = unit.matchups.groupVs.detectors.exists(u => u.pixelDistanceEdge(unit) < u.sightPixels + u.topSpeed * Seconds(8)())
    if ( ! unit.effectivelyCloaked || nearDetectionRange || nearMobileDetector || impendingTurret) {
      Potshot.delegate(unit)
      Retreat.delegate(unit)
    }
  }
}
