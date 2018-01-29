package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

object SpiderMine extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Vulture)                             &&
    With.self.hasTech(Terran.SpiderMinePlant)           &&
    unit.spiderMines > 0
  }
  
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
    if (With.grids.units.get(unit.tileIncludingCenter).exists(_.is(Terran.SpiderMine))) return
    
    val choke = unit.zone.edges.find(e => unit.pixelDistanceCenter(e.centerPixel) < e.radiusPixels)
    if (choke.isDefined) {
      placeMine(unit, unit.pixelCenter)
    }
  }
  
  protected def sabotage(vulture: FriendlyUnitInfo) {
    val rangeMinimum = if (vulture.matchups.doomedDiffused) 64.0 else 0.0
    val range = vulture.topSpeed * (vulture.matchups.framesToLiveCurrently - 12)
    val maxRangeTiles = PurpleMath.clamp(range, rangeMinimum, 8.0 * 32.0)
  
    //TODO: This is a good candidate for Coordinator since every Vulture will want to recalculate this
    val victims = vulture.matchups.targets.filterNot(_.unitClass.floats)
    if (victims.isEmpty) return
    
    val saboteursInitial = new mutable.PriorityQueue[UnitInfo]()(Ordering.by(v => victims.map(_.pixelDistanceEdge(v)).min))
    val saboteursFinal = saboteursInitial.take(victims.size).toVector
    
    if ( ! saboteursFinal.contains(vulture)) return
    
    val targeter = new Targeter(vulture)
    val targetPixel = TargetAOE.chooseTargetPixel(
      vulture,
      maxRangeTiles,
      vulture.subjectiveValue / 3.0,
      targeter.valueTarget)
    
    targetPixel.foreach(finalTarget => placeMine(vulture, finalTarget))
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
      
      val ownership = if (target.isFriendly) -3.0 else 1.0
      val output = target.subjectiveValue * multiplier * ownership
    
      output
    }
  }
}
