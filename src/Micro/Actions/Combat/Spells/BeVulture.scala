package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.UnitFilters.IsTank

object BeVulture extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Terran.Vulture(unit)
    && Terran.SpiderMinePlant()
    && unit.spiderMines > 0
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    sabotage(unit)
    layTrap(unit)
  }
  
  protected def placeMine(vulture: FriendlyUnitInfo, target: Pixel): Unit = {
    val targetWalkable = target.walkablePixel
    if (vulture.pixelDistanceEdge(targetWalkable) > 24) {
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
  
  private object TargetFilterTriggersMines extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.unitClass.triggersSpiderMines
  }
  
  protected def sabotage(vulture: FriendlyUnitInfo): Unit = {

    if (vulture.matchups.targetNearest.isEmpty) return

    val t = vulture.matchups.targetNearest.get

    var abort = false
    abort ||= ! t.unitClass.triggersSpiderMines
    abort ||= vulture.confidence11 > 0.2
    abort ||= ! vulture.agent.shouldFight
    abort ||= vulture.pixelsToGetInRange(t) > 0
    abort ||= t.tile.toRectangle.expand(3, 3).tiles.exists(_.units.exists(u => u.isFriendly && u.isAny(IsTank, Terran.Goliath, Terran.SCV)))

    if (abort) return

    Potshot.delegate(vulture)
    if (vulture.unready) return

    placeMine(vulture, t.pixel.project(vulture.pixel, 48))
  }
}
