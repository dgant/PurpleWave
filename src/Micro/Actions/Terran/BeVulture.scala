package Micro.Actions.Terran

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
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

    lazy val target = vulture.matchups.targetNearest
      .orElse(vulture.matchups.threatNearest.filter(Protoss.DarkTemplar))
      .filter(_.unitClass.triggersSpiderMines)

    if (target.isEmpty) return
    
    val t = target.get

    var abort = false
    abort ||= ! t.unitClass.triggersSpiderMines
    abort ||= t.subjectiveValue < vulture.subjectiveValue
    abort ||= vulture.confidence11 > 0.5
    abort ||= ! vulture.agent.shouldFight
    abort ||= vulture.pixelsToGetInRange(t) > 32
    abort ||= t.tile.toRectangle.expand(2, 2).tiles.exists(_.units.exists(u => u.isFriendly && u.isAny(IsTank, Terran.Goliath, Terran.SCV)))
    abort &&= ! Protoss.DarkTemplar(t) || ! t.effectivelyCloaked
    abort &&= ! t.isAny(Zerg.Egg, Zerg.LurkerEgg)

    if (abort) return

    Potshot.delegate(vulture)
    if (vulture.unready) return

    placeMine(vulture, t.pixel.project(vulture.pixel, 48))
  }
}
