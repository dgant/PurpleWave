package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.{Squad, UnitGroup}
import Utilities.?
import Utilities.UnitFilters.{IsTank, IsWarrior}

import scala.collection.mutable.ArrayBuffer

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    var output = unit.canMove && unit.unitClass.isDetector
    output ||= unit.flying && unit.unitClass.isBuilding
    output ||= (With.enemies.exists(_.isTerran)
      && unit.isAny(Terran.Wraith, Terran.Valkyrie, Terran.ScienceVessel)
      && unit.squad.exists(_.count(IsTank) > 0)
      && unit.matchups.targets.filter(IsTank).exists(t => t.matchups.threatDeepest.exists(_.inRangeToAttack(t)) && ! t.visible))
    output &&= groupSupported(unit).nonEmpty
    output
  }

  private def canEventuallyCloak(unit: UnitInfo): Boolean = unit.isAny(Terran.SpiderMine, Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Zerg.Lurker)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    // TODO: This logic is sloppy and doesn't handle our various use cases well
    if (unit.matchups.pixelsEntangled > -96 && unit.totalHealth < 400) {
      if ( ! IsWarrior(unit)) {
        unit.agent.redoubt.set(groupSupported(unit).map(_.attackCentroidKey))
        Retreat.delegate(unit)
      }
      return
    }

    val group     = groupSupported(unit).get
    val spooky    = detectionTarget(unit)
    val tank      = tankTarget(unit)
    val tankPixel = tank.map(t =>
      ?(t.matchups.engagedUpon,
        t.pixel,
        Maff.weightedCentroid(Circle(unit.sightPixels / 32)
          .map(t.tile.add)
          .map(t => (t.center, With.framesSince(t.lastSeen).toDouble)))))

    val goals = new ArrayBuffer[Pixel]
    spooky  .foreach(spook => goals += spook.projectFrames(48))
    tank    .foreach(tank =>  goals += tank.pixel)
    if (group.attackers.exists( ! _.flying)) {
      goals += group.attackCentroidKey
    }
    if (goals.isEmpty) return

    goals += unit.pixel
    if (unit.agent.isLeader) {
      goals += group.destinationNext
    } else {
      goals += Maff.minBy(group.battleEnemies.filterNot(_.visible).map(_.pixel))(_.pixelDistanceSquared(unit.pixel)).getOrElse(group.destinationNext)
    }

    unit.agent.decision.set(MicroPathing.pullTowards(unit.sightPixels, goals: _*))
    Commander.move(unit)
  }

  def enemiesToConsider(unit: FriendlyUnitInfo): Seq[Iterable[UnitInfo]] = Seq(
    unit.targetsAssigned.getOrElse(Seq.empty),
    unit.enemiesSquad,
    ?(unit.targetsAssigned.isDefined, Seq.empty, unit.enemiesBattle))

  def groupSupported(unit: FriendlyUnitInfo): Option[UnitGroup] = {
    unit.squad.orElse(Some(unit.matchups.groupOf).filter(g => g.attacksGround || g.attacksAir))
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
    val tank  = Maff.minBy(Maff.orElse(tanks.filter(_.matchups.pixelsEntangled >= 0), tanks))(_.pixelDistanceSquared(groupSupported(unit).get.attackCentroidKey))
    tank
  }
}
