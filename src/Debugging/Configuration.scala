package Debugging

class Configuration {
  
  val combatEvaluationDistanceTiles   = 15
  val battleSimulatorUnitLimit        = 40
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
  var visualizeHitPoints          = false
  var visualizeHeuristicMovement  = false
  var visualizeHeuristicTargeting = false
  var visualizePerformance        = true
  var visualizePerformanceDetails = true
  var visualizePlans              = false
  var visualizeResources          = false
  var visualizeRealEstate         = false
  var visualizeScheduler          = false
  var visualizeSimulation         = false
  var visualizeTextOnly           = false
  var visualizeUnitsForeign       = false
  var visualizeUnitsOurs          = false
  var visualizeVectorUnits        = false
  
  var gameSpeed                   = 0
  var enableSurrendering          = true
  var enableStdOut                = false
  var enableChat                  = true
  var enableLatencyCompensation   = false
  
  var camera                      = true
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
  
  var maxFrameMilliseconds  = 20
  
  var urgencyManners        = 1
  var urgencyEconomy        = 1
  var urgencyGeography      = 1
  var urgencyPlanning       = 2
  var urgencyBattles        = 5
  var urgencyGrids          = 10
  var urgencyUnitTracking   = 10
  var urgencyMicro          = 100
}
