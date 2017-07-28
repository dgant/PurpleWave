package Information.Grids.Dpf

import Information.Grids.ArrayTypes.AbstractGridDouble
import Lifecycle.With
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDpf extends AbstractGridDouble {
  
  //Range is calculated from unit edge to unit edge.
  //Let's make sure we account for the hypotenuse of even a big target like a Dragoon
  private lazy val assumedTargetRadialHypotenuse = Protoss.Dragoon.radialHypotenuse
  
  //We want to know about the damage in the next tile over
  private lazy val safetyRange = 16.0
  
  override def update() {
    
    reset()
    
    val framesToLookAhead = 24
    val cooldownPenalty = With.configuration.dpfGridCooldownPenalty
    val distancePenalty = With.configuration.dpfGridDistancePenalty
    val movementPenalty = With.configuration.dpfGridMovementPenalty
    
    getUnits.foreach(unit => {
      var dpf = if (air) unit.airDpf else unit.groundDpf
      if (dpf > 0.0) {
        var pixelImpactMax  = if (air) unit.pixelReachAir(framesToLookAhead) else unit.pixelReachGround(framesToLookAhead)
        var pixelRangeMax   = if (air) unit.pixelRangeAir else unit.pixelRangeGround
        var pixelRangeMin   = unit.unitClass.groundMinRangeRaw
        
        //Assume invisible siege tanks are sieged
        if ( ! unit.isOurs && ! unit.visible && unit.is(Terran.SiegeTankUnsieged)) {
          dpf               = dpf * 2 //Hack adjustment
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
            dpf *= 1.0 - cooldownPenalty * Math.max(0.0, cooldownLeft/cooldown)
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
              val adjustedDpf = dpf -
                distancePenalty * point.lengthSquared * distancePenaltyRatio -
                movementPenalty * point.lengthSquared * movementPenaltyRatio
              if (adjustedDpf > 0) {
                add(nearbyTile, adjustedDpf)
              }
            }
          })
      }
    })
  }
  
  protected val air:Boolean
  protected def getUnits:Iterable[UnitInfo]
}
