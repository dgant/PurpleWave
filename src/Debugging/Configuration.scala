package Debugging

class Configuration {
  
  // Up this for tournament play!
  var maxFrameMilliseconds = 20
  
  val enableBattleSimulation          = false
  val enableBattleEstimation          = true
  val battleSimulationUnitLimit       = 9999
  val battleWorkerCostPerFrame        = 0.15
  val battleMarginTiles               = 15
  
  val dpsGridDistancePenalty          = 0.5
  val dpsGridMovementPenalty          = 0.5
  val dpsGridCooldownPenalty          = 0.25
  
  val microFrameLookahead             = 8
  
  var enableFastGroundDistance        = true
  var enablePathRecalculation         = true
  var pathRecalculationDelayFrames    = 12
  var woundedThresholdHealth          = 40
  
  var visualize                   = true
  var visualizeBases              = false
  var visualizeBattles            = true
  var visualizeBattleSimulation   = true
  var visualizeBattleTacticsRanks = false
  var visualizeBullets            = false
  var visualizeChokepoints        = false
  var visualizeEconomy            = false
  var visualizeGeography          = false
  var visualizeGrids              = false
  var visualizeHitPoints          = true
  var visualizeHeuristicMovement  = true
  var visualizePerformance        = false
  var visualizePerformanceDetails = false
  var visualizePlans              = false
  var visualizeResources          = false
  var visualizeRealEstate         = false
  var visualizeScheduler          = false
  var visualizeTextOnly           = false
  var visualizeUnitsForeign       = true
  var visualizeUnitsOurs          = false
  var visualizeVectorUnits        = false
  
  var gameSpeed                   = 0
  var enableSurrendering          = true
  var enableStdOut                = false
  var enableChat                  = true
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
  
  var urgencyManners        = 1
  var urgencyEconomy        = 1
  var urgencyGeography      = 1
  var urgencyPlanning       = 2
  var urgencyBattles        = 5
  var urgencyGrids          = 10
  var urgencyUnitTracking   = 10
  var urgencyMicro          = 100
}
