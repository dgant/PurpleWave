package Micro.Actions.Combat.Spells

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Filters.TargetFilter
import Micro.Actions.Combat.Attacking.TargetAction
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

object SpiderMine extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.Vulture)
    && With.self.hasTech(Terran.SpiderMinePlant)
    && unit.spiderMines > 0
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    layTrap(unit)
    sabotage(unit)
  }
  
  protected def placeMine(unit: FriendlyUnitInfo, target: Pixel) {
    val finalTarget = calculateMineTarget(unit, target)
    With.commander.useTechOnPixel(unit, Terran.SpiderMinePlant, finalTarget)
  }
  
  protected def calculateMineTarget(unit: FriendlyUnitInfo, target: Pixel): Pixel = {
    if (unit.pixelDistanceCenter(target) > 32) return target
    if ( ! unit.moving) return target
    val velocity = unit.velocity.normalize(6)
    unit.pixelCenter.add(velocity.x.toInt, velocity.y.toInt)
  }
  
  protected def layTrap(unit: FriendlyUnitInfo) {
    if (unit.spiderMines < 2) return
    if (unit.matchups.framesOfSafetyDiffused <= 0) return
    
    lazy val inChoke = unit.zone.edges.exists(e => unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels)
    lazy val mineSpace = ! unit.tileArea.expand(1, 1).tiles.exists(With.grids.units.get(_).exists(_.is(Terran.SpiderMine)))
    lazy val retreating = unit.matchups.threats.nonEmpty && ! unit.agent.shouldEngage
    lazy val timeToMine  = unit.matchups.framesOfSafetyDiffused > GameTime(0, 2)()
    lazy val inWorkerLine = unit.base.exists(base => base.owner.isUs && base.harvestingArea.contains(unit.tileIncludingCenter))
    if (mineSpace
      && timeToMine
      && ! inWorkerLine
      && ((inChoke && unit.spiderMines == 3) || retreating)) {
      placeMine(unit, unit.pixelCenter)
    }
  }
  
  private object TargetFilterTriggersMines extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.unitClass.triggersSpiderMines
  }
  
  protected def sabotage(vulture: FriendlyUnitInfo) {
    if (vulture.cooldownLeft <= 0) return
    val rangeMinimum = if (vulture.matchups.doomedDiffused) 64.0 else 0.0
    val range = vulture.topSpeed * (vulture.matchups.framesToLiveCurrently - 12)
    val maxRangeTiles = PurpleMath.clamp(range, rangeMinimum, 8.0 * 32.0)
  
    //TODO: This is a good candidate for Coordinator since every Vulture will want to recalculate this
    val victims = vulture.matchups.enemies.filter(_.unitClass.triggersSpiderMines)
    if (victims.isEmpty) return
    
    // TODO: Also capture other threats in range of targets like siege tanks
    val saboteurs = vulture.matchups.alliesInclSelf.filter(u => u.is(Terran.Vulture) && u.friendly.exists(_.spiderMines > 0))
    val minesweepers = vulture.matchups.threats.count(_.pixelRangeGround > 32 * 4)
    if (saboteurs.size < minesweepers) return
    
    val saboteursInitial  = new mutable.PriorityQueue[UnitInfo]()(Ordering.by(v => victims.map(_.pixelDistanceEdge(v)).min)) ++ saboteurs
    val saboteursFinal    = saboteursInitial.take(2 * victims.size).toVector
    if ( ! saboteursFinal.contains(vulture)) return
    
    new TargetAction(TargetFilterTriggersMines).delegate(vulture)
    val mineDistance = 20 + vulture.unitClass.radialHypotenuse
    val targetPixel = vulture.agent.toAttack.map(target => target.pixelCenter.project(vulture.pixelCenter, mineDistance))
    targetPixel.foreach(placeMine(vulture, _))
    vulture.agent.toAttack = None
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
