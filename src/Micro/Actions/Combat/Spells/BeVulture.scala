package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Micro.Targeting.TargetFilter
import Utilities.UnitFilters.{IsTank, IsWorker}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.Seconds

import scala.collection.mutable

object BeVulture extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.Vulture)
    && With.self.hasTech(Terran.SpiderMinePlant)
    && unit.spiderMines > 0
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    layTrap(unit)
    sabotage(unit)
  }
  
  protected def placeMine(unit: FriendlyUnitInfo, target: Pixel): Unit = {
    val finalTarget = calculateMineTarget(unit, target)
    Commander.useTechOnPixel(unit, Terran.SpiderMinePlant, finalTarget)
  }
  
  protected def calculateMineTarget(unit: FriendlyUnitInfo, target: Pixel): Pixel = {
    if (unit.pixelDistanceCenter(target) > 32) return target
    if ( ! unit.moving) return target
    val velocity = unit.velocity.normalize(6)
    unit.pixel.add(velocity.x.toInt, velocity.y.toInt)
  }
  
  protected def layTrap(unit: FriendlyUnitInfo): Unit = {
    if (unit.spiderMines < 2) return
    if (unit.matchups.framesOfSafety <= 0) return
    
    lazy val inChoke = unit.zone.edges.exists(e => unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels)
    lazy val mineSpace = ! unit.tileArea.expand(1, 1).tiles.exists(_.units.exists(Terran.SpiderMine))
    lazy val retreating = unit.matchups.threats.nonEmpty && ! unit.agent.shouldFight
    lazy val timeToMine  = unit.matchups.framesOfSafety > Seconds(2)()
    lazy val inWorkerLine = unit.base.exists(base => base.owner.isUs && base.harvestingArea.contains(unit.tile))
    if (mineSpace
      && timeToMine
      && ! inWorkerLine
      && ((inChoke && unit.spiderMines == 3) || retreating)) {
      placeMine(unit, unit.pixel)
    }
  }
  
  private object TargetFilterTriggersMines extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.unitClass.triggersSpiderMines
  }
  
  protected def sabotage(vulture: FriendlyUnitInfo): Unit = {
    // TODO: Do it if we have enough Vultures with mines
    if ( ! vulture.agent.shouldFight && ! vulture.matchups.enemies.exists(e => e.is(Protoss.DarkTemplar) && e.effectivelyCloaked)) return

    val victims = vulture.matchups.enemies.filter(e =>
      e.unitClass.triggersSpiderMines
      && ! e.matchups.threatsInRange.exists(t => t.isAny(IsTank, IsWorker) && t.pixelDistanceEdge(e) < 32 * 5))

    vulture.agent.toAttack = Maff.minBy(victims)(_.pixelDistanceEdge(vulture))
    if (vulture.agent.toAttack.isEmpty) return
    val target = vulture.agent.toAttack.get

    Potshot.delegate(vulture)
    if (vulture.unready) return

    val saboteurs = vulture.matchups.alliesInclSelf.filter(u => u.is(Terran.Vulture) && u.friendly.exists(_.spiderMines > 0))
    val saboteursInitial  = new mutable.PriorityQueue[UnitInfo]()(Ordering.by(v => victims.map(_.pixelDistanceEdge(v)).min)) ++ saboteurs
    val saboteursFinal    = saboteursInitial.take(2 * victims.size).toVector
    if ( ! saboteursFinal.contains(vulture)) return

    if (vulture.pixelDistanceEdge(target) < 32) {
      placeMine(vulture, vulture.projectFrames(1))
    } else {
      vulture.agent.decision.set(
        vulture.pixel
          .project(target.pixel, vulture.pixelDistanceEdge(target) + 64)
          .walkableTile
          .center)
      Commander.move(vulture)
    }
  }
  
  private class Targeter(vulture: FriendlyUnitInfo) {
    def valueTarget(target: UnitInfo): Double = {
      if (target.unitClass.isBuilding)  return 0.0
      if (target.invincible)            return 0.0
      val multiplier =
        if (target == vulture)
          0.0
        else if (target.flying)
          0.0
        else if (target.unitClass.isBuilding)
          0.0
        else
          1.0
      
      val ownership =
        if (target.isFriendly)
          if (target.is(Terran.Vulture))
            0.0
          else
            -3.0
        else
          1.0
      val output = target.subjectiveValue * multiplier * ownership
    
      output
    }
  }
}
