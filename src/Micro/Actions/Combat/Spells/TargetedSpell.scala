package Micro.Actions.Combat.Spells

import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Ring
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Heuristics.{SpellTargetAOE, SpellTargetSingle}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class TargetedSpell extends Action {
  
  protected def casterClass       : UnitClass
  protected def tech              : Tech
  protected def aoe               : Boolean
  protected def castRangeTiles    : Int
  protected def thresholdValue    : Double
  protected def lookaheadPixels   : Int = 0
  protected def pixelWidth        : Int = 96
  protected def pixelHeight       : Int = 96
  protected def bonusSearchPixels : Int = 32

  final def castRangePixels: Int = Maff.x32(castRangeTiles)
  
  protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double

  def hasEnoughEnergy(unit: UnitInfo): Boolean = unit.energy >= tech.energyCost
  def canCast(unit: UnitInfo): Boolean = casterClass(unit) && unit.player.hasTech(tech) && hasEnoughEnergy(unit)
  final override def allowed(unit: FriendlyUnitInfo): Boolean = canCast(unit) && unit.matchups.enemies.nonEmpty && additionalConditions(unit)
  
  protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = true
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val safeDistance  = Math.max(0, -unit.matchups.pixelsEntangled)
    val totalRange    = safeDistance + 32.0 * castRangeTiles + bonusSearchPixels // The margin is just some encouragement
    
    if (aoe) {
      val targetPixel = new SpellTargetAOE().chooseTargetPixel(unit, totalRange, thresholdValue, valueTarget, pixelWidth = pixelWidth, pixelHeight = pixelHeight)
      targetPixel.foreach(target => {
        if (unit.pixelDistanceCenter(target) <= castRangePixels) {
          targetPixel.foreach(Commander.useTechOnPixel(unit, tech, _))
          targetPixel.foreach(onCast(unit, _))
        } else {
          moveInRange(unit, target)
        }
      })
    } else {
      val targetUnit = SpellTargetSingle.chooseTarget(unit, totalRange, thresholdValue, valueTarget)
      targetUnit.foreach(target => {
        if (unit.pixelDistanceEdge(target) <= castRangePixels) {
          targetUnit.foreach(Commander.useTechOnUnit(unit, tech, _))
        } else {
          moveInRange(unit, target.pixel)
        }
      })
    }
  }
  
  // Event handler for when the unit issues a cast
  protected def onCast(caster: FriendlyUnitInfo, target: Pixel): Unit = {}

  private def moveInRange(caster: FriendlyUnitInfo, target: Pixel): Unit ={
    if (caster.flying || caster.transport.exists(_.flying)) {
      caster.agent.toTravel = Some(target)
    } else {
      caster.agent.toTravel = Maff.minBy(Ring(castRangeTiles).map(target.tile.add).map(_.center))(caster.pixelDistanceTravelling)
    }
    Commander.move(caster)
  }
}
