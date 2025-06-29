package Micro.Actions.Terran

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.LightYear
import Utilities.UnitFilters.IsTank

object BeCombatSCV extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Terran.SCV(unit)
    && unit.intent.toBuild      .isEmpty
    && unit.intent.toGather     .isEmpty
    && unit.intent.toHeal       .isEmpty
    && unit.intent.toScoutTiles .isEmpty
    && unit.squad               .isDefined
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if ( ! unit.squad.exists(_.repairables.nonEmpty)) return

    unit.intent.canFight = false

    val vips = unit.squad.get.repairableVIPs.flatMap(_.friendly).toVector
      .sortBy(t => t.pixelDistanceSquared(unit) + t.matchups.pixelsToThreatRange.getOrElse(LightYear().toDouble))
      .sortBy(IsTank)
      .sortBy(Terran.Valkyrie)
      .sortBy(Terran.Battlecruiser)
      .sortBy(_.healers.count(_ != unit))

    if (vips.isEmpty) return

    val vip = vips.head
    With.coordinator.healing.heal(unit, vip)

    if ( ! vip.flying || ! vip.sieged) {
      unit.agent.station.set(unit.matchups.threatDeepest.map(t => t.pixel.project(vip.pixel, t.pixelRangeAgainst(unit) + 64)).getOrElse(vip.agent.destinationNext().project(unit.pixel, 64)))
      unit.agent.forces.put(Forces.travel,  Potential.towardsDestination(unit))
      unit.agent.forces.put(Forces.threat,  Potential.softAvoidThreatRange(unit))
      unit.agent.forces.put(Forces.spacing, Potential.preferSpacing(unit))
      unit.agent.forces.put(Forces.pushing, Potential.followPushes(unit))
      MicroPathing.setWaypointForcefully(unit)
    }
  }
  
  protected def placeMine(vulture: FriendlyUnitInfo, target: Pixel): Unit = {
    val targetWalkable = target.walkablePixel
    if (vulture.pixelDistanceCenter(targetWalkable) > 16) {
      vulture.agent.decision.set(targetWalkable)
      Commander.move(vulture)
    } else if (vulture.speed > 0) {
      Commander.hold(vulture)
    } else {
      Commander.useTechOnPixel(vulture, Terran.SpiderMinePlant, targetWalkable)
    }
  }
  
  protected def layTrap(vulture: FriendlyUnitInfo): Unit = {
    if (vulture.spiderMines < 2) return
    if (vulture.matchups.pixelsToThreatRange.exists(_ < 256)) return
    if (vulture.id % 4 != Maff.div128(With.frame) % 4) return
    val target = Spiral.apply(5).map(vulture.tile.add).find(tile =>
      tile.walkable
      && ( ! tile.buildable || ! tile.base.exists(_.isOurs) || tile.zone.edges.exists(_.contains(tile)))
      && tile.units.forall(vulture==)
      && ! tile.toRectangle.expand(2).tiles.exists(_.units.exists(IsTank)))
    target.foreach(t => placeMine(vulture, t.center))
  }
  
  protected def sabotage(vulture: FriendlyUnitInfo): Unit = {

    lazy val t = vulture.matchups.targetNearest
      .orElse(vulture.matchups.threatNearest.filter(_.effectivelyCloaked))
      .filter(_.unitClass.triggersSpiderMines)

    if (t.isEmpty) return

    var abort = false
    abort ||= ! t.get.unitClass.triggersSpiderMines
    abort ||= t.get.subjectiveValue > vulture.subjectiveValue
    abort ||= vulture.confidence11 > 0.5
    abort ||= ! vulture.agent.shouldFight
    abort ||= vulture.pixelsToGetInRange(t.get) > 32
    abort ||= t.get.tile.toRectangle.expand(2, 2).tiles.exists(_.units.exists(u => u.isFriendly && u.isAny(IsTank, Terran.Goliath, Terran.SCV)))

    if (abort) return

    Potshot.delegate(vulture)
    if (vulture.unready) return

    placeMine(vulture, t.get.pixel.project(vulture.pixel, 48))
  }
}
