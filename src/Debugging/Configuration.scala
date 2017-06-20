package Debugging

class Configuration {
  
  ///////////////
  // Operation //
  ///////////////
  
  var gameSpeed          = 0
  var enableSurrendering = true
  var enableStdOut       = false // StdOut crashes bots on SSCAIT because it blocks forever
  var enableChat         = true
  
  ///////////
  // Micro //
  ///////////
  
  var attackableRangeBuffer           = 8
  var fogPositionDuration             = 24 * 99999
  var violenceFrameThreshold          = 24
  var enableYolo                      = true
  var enablePathRecalculation         = true
  var enableMineralWalk               = false
  var pathRecalculationDelayFrames    = 48
  var woundedThresholdHealth          = 40
  var evaluateDangerousBases          = false
  
  var dpsGridDistancePenalty          = 0.5
  var dpsGridMovementPenalty          = 0.5
  var dpsGridCooldownPenalty          = 0.25
  var bunkerSafetyMargin              = Math.sqrt(16.0 * 16.0 * 2.0) + Math.sqrt(32.0 * 16.0 * 2.0) + 32.0 * (4.0 + 1.0) + /* This is the margin! */ 8.0
  
  ///////////
  // Macro //
  ///////////
  
  var maxBuildingPlacementCandidates  = 300
  var maxMineralsBeforeMinedOut       = 300 * 8
  var blockerMineralThreshold         = 50
  var maxPlacementAge                 = 24 * 20
  
  /////////////
  // Battles //
  /////////////
  
  var battleWorkerCostPerFrame  = 0.25
  var battleMarginTiles         = 15
  var battleMarginPixels        = battleMarginTiles * 32.0
  var battleEstimationFrames    = 24 * 5
  
  /////////////////
  // Performance //
  /////////////////
  
  var peformanceFrameMilliseconds = 20
  var performanceMinimumUnitSleep = 2
  var performanceMicroAngleStep   = 4
  var useFastGroundDistance       = true
  var unitHistoryAge              = 24 * 3
  
  var urgencyManners            = 1
  var urgencyEconomy            = 1
  var urgencyGeography          = 1
  var urgencyBuildingPlacement  = 2
  var urgencyPlanning           = 2
  var urgencyBattles            = 3
  var urgencyGrids              = 5
  var urgencyUnitTracking       = 5
  var urgencyMicro              = 100
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualize                           = true
  var visualizationProbabilityHappyVision = 0.1
  var visualizationProbabilityTextOnly    = 0.02
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
}
