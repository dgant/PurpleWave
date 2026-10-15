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
import Tactic.Squads.UnitGroup
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
    if (unit.matchups.threats.exists(shouldRetreatFrom(unit, _)) && unit.totalHealth < 400
        && ( ! Protoss.Observer(unit)
        || unit.matchups.enemyDetectorDeepest.exists(d => d.canMove || d.pixelsToSightRange(unit) < 64)
        || With.units.existsEverEnemy(Terran.Academy, Terran.Comsat, Terran.SpellScannerSweep))) {
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

    unit.agent.decision.set(spottingPosition(unit, spooky, goals))
    Commander.move(unit)
  }

  private[Micro] def shouldRetreatFrom(unit: FriendlyUnitInfo, threat: UnitInfo): Boolean = {
    // Only detection justifies closing inside the normal safety buffer.
    if (unit.unitClass.isDetector && threat.cloakedOrBurrowed) {
      unit.pixelDistanceCenter(threat) < Math.min(unit.detectionRangePixels - 32, threat.pixelRangeAgainst(unit) + 96)
    } else {
      unit.pixelDistanceEdge(threat) < threat.pixelRangeAgainst(unit) + 96
    }
  }

  private[Micro] def spottingPosition(unit: FriendlyUnitInfo, spooky: Option[UnitInfo], goals: Seq[Pixel]): Pixel = {
    val preferred = MicroPathing.pullTowards(unit.sightPixels, goals: _*)
    // Keep current detection even when prediction or secondary goals pull us away.
    // Leave 16px between the desired coverage boundary and the capped retreat trigger.
    spooky.filter(_.cloakedOrBurrowed).map(t => t.pixel.projectUpTo(preferred, Math.max(0, unit.detectionRangePixels - 16))).getOrElse(preferred)
  }

  def enemiesToConsider(unit: FriendlyUnitInfo): Seq[Iterable[UnitInfo]] = Seq(
    unit.targetsAssigned.getOrElse(Seq.empty),
    unit.enemiesSquad,
    ?(unit.targetsAssigned.isDefined, Seq.empty, unit.enemiesBattle))

  def groupSupported(unit: FriendlyUnitInfo): Option[UnitGroup] = {
    ?(
      unit.isAny(Terran.ScienceVessel, Protoss.Observer),
      unit.squad.orElse(Some(unit.matchups.groupOf).filter(g => g.attacksGround || g.attacksAir)),
      unit.squad)
  }

  def detectionTarget(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    if ( ! unit.unitClass.isDetector) return None
    val mobileDetectors = groupSupported(unit).map(_.mobileDetectors).getOrElse(Seq.empty).filter(_.friendly.exists(_.squad == unit.squad))
    val spookies = Maff.orElseFiltered(enemiesToConsider(unit): _*)(e =>
        (e.cloakedOrBurrowed || canEventuallyCloak(e))
        && (unit.squad.exists(_.canAttackIfVisible(e)) || unit.matchups.groupOf.canAttackIfVisible(e)))
    val spookiesResponsible = spookies.filter(spooky =>
      spooky.matchups.enemyDetectorDeepest
        .filter(d => d.canMove && d.friendly.exists(_.squad == unit.squad))
        .map(_ == unit)
        .getOrElse(Maff.minBy(mobileDetectors)(_.pixelDistanceSquared(spooky)).contains(unit)))
    Maff.minBy(spookiesResponsible)(_.pixelDistanceSquared(unit))
  }

  def tankTarget(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    if ( ! unit.squad.filter(groupSupported(unit).contains).exists(_.attacksGround)) return None
    val tanks = Maff.orElseFiltered(enemiesToConsider(unit): _*)(IsTank)
    val tank  = Maff.minBy(Maff.orElse(tanks.filter(_.matchups.pixelsEntangled >= 0), tanks))(_.pixelDistanceSquared(groupSupported(unit).get.attackCentroidKey))
    tank
  }
}
