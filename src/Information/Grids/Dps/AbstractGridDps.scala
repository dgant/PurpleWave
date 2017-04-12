package Information.Grids.Dps

import Mathematics.Shapes.Circle
import Information.Grids.ArrayTypes.AbstractGridDouble
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With
import Utilities.EnrichPosition._

abstract class AbstractGridDps extends AbstractGridDouble {
  
  //Range is calculated from unit edge to unit edge.
  //Let's make sure we account for the hypotenuse of even a fat target like a Dragoon
  private lazy val assumedTargetRadialHypotenuse = Protoss.Dragoon.radialHypotenuse
  
  //We want to know about the damage in the next tile over
  private lazy val safetyRange = 16.0
  
  override def update() {
    
    reset()
    
    val framesToLookAhead = 24 + With.performance.cacheLength(With.grids.frameDelayScale)
    
    getUnits.foreach(unit => {
      var dps = if (air) unit.unitClass.airDps else unit.unitClass.groundDps
      if (dps > 0.0) {
        var pixelReachMax = if (air) unit.pixelReachAir(framesToLookAhead) else unit.pixelReachGround(framesToLookAhead)
        var pixelRangeMax = if (air) unit.pixelRangeAir else unit.pixelRangeGround
        var pixelRangeMin = unit.unitClass.rawGroundMinRange
        
        //Assume invisible siege tanks are sieged
        if ( ! unit.isOurs && ! unit.visible && unit.unitClass == Terran.SiegeTankUnsieged) {
          dps             = Math.max(dps,           Terran.SiegeTankSieged.groundDps)
          pixelReachMax   = Math.max(pixelReachMax, Terran.SiegeTankSieged.groundRange)
          pixelRangeMax   = Math.max(pixelRangeMax, Terran.SiegeTankSieged.groundRange)
        }
        
        //Account for edge-to-edge distance
        val margin = safetyRange + assumedTargetRadialHypotenuse + unit.unitClass.radialHypotenuse
        pixelReachMax += margin
        pixelRangeMax += margin
  
        val distancePenalty = With.configuration.combatDistancePenalty
        val movementPenalty = With.configuration.combatMovementPenalty
        val cooldownPenalty = With.configuration.combatCooldownPenalty
        
        if (cooldownPenalty > 0 && unit.cooldownLeft > 0) {
          val cooldown: Double = if (air) unit.unitClass.airDamageCooldown else unit.unitClass.groundDamageCooldown
          if (cooldown > 0) {
            val cooldownLeft: Double = if (air) unit.airCooldownLeft else unit.groundCooldownLeft
            dps *= 1.0 - cooldownPenalty * Math.max(0.0, cooldownLeft/cooldown)
          }
        }
  
        Circle
          .points((pixelReachMax/32).toInt)
          .foreach(point => {
            val nearbyTile = unit.tileIncludingCenter.add(point)
            if (nearbyTile.valid) {
              var adjustedDps = dps
              if (distancePenalty > 0) {
                adjustedDps -= adjustedDps * distancePenalty * point.lengthSquared * 32.0 * 32.0 / (pixelReachMax * pixelReachMax)
              }
              if (movementPenalty > 0 && point.lengthSquared * 32.0 * 32.0 < pixelRangeMax  * pixelRangeMax) {
                adjustedDps *= movementPenalty
              }
              add(nearbyTile, adjustedDps)
            }
          })
      }
    })
  }
  
  protected val air:Boolean
  protected def getUnits:Iterable[UnitInfo]
}
