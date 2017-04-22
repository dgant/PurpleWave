package Debugging

class Configuration {
  
  val combatEvaluationDistanceTiles   = 15
  val dpsGridDistancePenalty          = 0.5
  val dpsGridMovementPenalty          = 0.5
  val dpsGridCooldownPenalty          = 0.25
  val combatStickinessLeash           = 64.0
  var enableFastGroundDistance        = true
  var enablePathRecalculation         = true
  var pathRecalculationDelayFrames    = 240
  
  var visualize                   = true
  var visualizeBases              = false
  var visualizeBattles            = true
  var visualizeBullets            = false
  var visualizeChokepoints        = false
  var visualizeEconomy            = false
  var visualizeGeography          = false
  var visualizeGrids              = false
  var visualizeHitPoints          = true
  var visualizeHeuristicMovement  = true
  var visualizeHeuristicTargeting = true
  var visualizePerformance        = false
  var visualizePerformanceDetails = true
  var visualizePlans              = false
  var visualizeResources          = false
  var visualizeRealEstate         = false
  var visualizeScheduler          = false
  var visualizeSimulation         = true
  var visualizeTextOnly           = false
  var visualizeUnitsForeign       = true
  var visualizeUnitsOurs          = false
  var visualizeVectorUnits        = false
  
  var gameSpeed                   = 0
  var enableSurrendering          = true
  var enableStdOut                = false
  var enableChat                  = true
  var enableLatencyCompensation   = false
  
  var camera                      = true
  var cameraDynamicSpeed          = true
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
  
  var maxFrameMilliseconds  = 25
  
  var urgencyManners        = 1
  var urgencyEconomy        = 2
  var urgencyBattles        = 3
  var urgencyGeography      = 4
  var urgencyPlanning       = 6
  var urgencyMicro          = 7
  var urgencyGrids          = 8
  var urgencyUnitTracking   = 10
}
