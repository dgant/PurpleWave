package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.UnitGroup
import Utilities.UnitFilters.IsTank

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    var output = unit.canMove && unit.unitClass.isDetector
    output ||= unit.flying && unit.unitClass.isBuilding
    output ||= (unit.isAny(Terran.Wraith, Terran.Valkyrie, Terran.ScienceVessel)
      && unit.squad.exists(_.count(IsTank) > 0)
      && unit.matchups.targets.filter(IsTank).exists(t => t.matchups.threatDeepest.exists(_.inRangeToAttack(t)) && ! t.visible))
    output &&= groupSupported(unit).nonEmpty
    output
  }

  private def canEventuallyCloak(unit: UnitInfo): Boolean = unit.isAny(Terran.SpiderMine, Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Zerg.Lurker)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val from    = groupSupported(unit).get.centroidKey
    val to      = groupSupported(unit).get.stepConsensus
    val mid     = from.project(to, unit.sightPixels).tile
    val spooky  = detectionTarget(unit)
    val tank    = tankTarget(unit)

    unit.agent.toTravel =
      if (spooky.isDefined) {
        spooky.map(_.projectFrames(48))
      } else if (tank.exists(s => s.matchups.threatDeepest.exists(_.inRangeToAttack(s)))) {
        tank.map(_.pixel)
      } else {
        Some(Maff.weightedCentroid(Circle(8)
          .map(tank.map(_.tile).getOrElse(mid).add)
          .map(t => (t.center, With.framesSince(t.lastSeen).toDouble))))
      }

    if (unit.matchups.pixelsEntangled > -96) {
      unit.agent.toReturn = groupSupported(unit).map(_.attackCentroidKey)
      Retreat.delegate(unit)
    } else {
      Commander.move(unit)
    }
  }

  def enemiesToConsider(unit: FriendlyUnitInfo): Seq[Iterable[UnitInfo]] = Seq(
    unit.targetsAssigned.getOrElse(Seq.empty),
    unit.enemiesSquad,
    unit.enemiesBattle)

  def groupSupported(unit: FriendlyUnitInfo): Option[UnitGroup] = {
    (unit.squad.toSeq :+ unit.matchups.groupOf).find(g => g.attacksGround || g.attacksAir)
  }

  def detectionTarget(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    if ( ! unit.unitClass.isDetector) return None
    val spookies = Maff.orElseFiltered(enemiesToConsider(unit): _*)(e =>
        (e.cloakedOrBurrowed || canEventuallyCloak(e))
        && (unit.squad.exists(_.canAttackIfVisible(e)) || unit.matchups.groupOf.canAttackIfVisible(e)))
    val spookiesResponsible = spookies.filter(_.matchups.enemyDetectorDeepest.forall(unit==))
    Maff.minBy(spookiesResponsible)(_.pixelDistanceSquared(unit))
  }

  def tankTarget(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    if ( ! unit.squad.filter(groupSupported(unit).contains).exists(_.attacksGround)) return None
    val tanks = Maff.orElseFiltered(enemiesToConsider(unit): _*)(IsTank)
    val tank = Maff.minBy(Maff.orElse(tanks.filter(_.matchups.pixelsEntangled >= 0), tanks))(_.pixelDistanceSquared(groupSupported(unit).get.attackCentroidKey))
    tank
  }
}
