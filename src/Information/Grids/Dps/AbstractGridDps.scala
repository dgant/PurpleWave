package Information.Grids.Dps

import Information.Grids.ArrayTypes.AbstractGridDouble
import Lifecycle.With
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDps extends AbstractGridDouble {
  
  //Range is calculated from unit edge to unit edge.
  //Let's make sure we account for the hypotenuse of even a big target like a Dragoon
  private lazy val assumedTargetRadialHypotenuse = Protoss.Dragoon.radialHypotenuse
  
  //We want to know about the damage in the next tile over
  private lazy val safetyRange = 16.0
  
  override def update() {
    
    reset()
    
    val framesToLookAhead = 24
    val cooldownPenalty = With.configuration.dpsGridCooldownPenalty
    val distancePenalty = With.configuration.dpsGridDistancePenalty
    val movementPenalty = With.configuration.dpsGridMovementPenalty
    
    getUnits.foreach(unit => {
      var dps = if (air) unit.airDps else unit.groundDps
      if (dps > 0.0) {
        var pixelImpactMax  = if (air) unit.pixelImpactAir(framesToLookAhead) else unit.pixelImpactGround(framesToLookAhead)
        var pixelRangeMax   = if (air) unit.pixelRangeAir else unit.pixelRangeGround
        var pixelRangeMin   = unit.unitClass.groundMinRangeRaw
        
        //Assume invisible siege tanks are sieged
        if ( ! unit.isOurs && ! unit.visible && unit.is(Terran.SiegeTankUnsieged)) {
          dps               = Math.max(dps,             Terran.SiegeTankSieged.groundDps)
          pixelImpactMax    = Math.max(pixelImpactMax,  Terran.SiegeTankSieged.groundRange)
          pixelRangeMax     = Math.max(pixelRangeMax,   Terran.SiegeTankSieged.groundRange)
        }
        
        //Account for edge-to-edge distance
        //Then add a smidge extra to account for derpy pathing and approximated distance formulas
        val edgeMargin = (assumedTargetRadialHypotenuse + unit.unitClass.radialHypotenuse).toInt
        val safeMargin = edgeMargin + safetyRange
        pixelImpactMax  += safeMargin
        pixelRangeMax   += safeMargin
        pixelRangeMin   += edgeMargin
        
        if (cooldownPenalty > 0 && unit.cooldownLeft > 0) {
          val cooldown: Double = if (air) unit.unitClass.airDamageCooldown else unit.unitClass.groundDamageCooldown
          if (cooldown > 0) {
            val cooldownLeft: Double = if (air) unit.airCooldownLeft else unit.groundCooldownLeft
            dps *= 1.0 - cooldownPenalty * Math.max(0.0, cooldownLeft/cooldown)
          }
        }
  
        val tileCenter = unit.project(framesToLookAhead).tileIncluding
        val distancePenaltyRatio = Math.pow(32.0 / pixelImpactMax, 2)
        val movementPenaltyRatio = Math.pow(32.0 / pixelRangeMax, 2)
        
        Circle
          .points((pixelImpactMax/32).toInt)
          .foreach(point => {
            val nearbyTile = tileCenter.add(point)
            if (nearbyTile.valid) {
              val adjustedDps = dps -
                distancePenalty * point.lengthSquared * distancePenaltyRatio -
                movementPenalty * point.lengthSquared * movementPenaltyRatio
              if (adjustedDps > 0) {
                add(nearbyTile, adjustedDps)
              }
            }
          })
      }
    })
  }
  
  protected val air:Boolean
  protected def getUnits:Iterable[UnitInfo]
}
