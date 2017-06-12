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
  var microFrameLookahead             = 8
  var enableYolo                      = true
  var enablePathRecalculation         = true
  var pathRecalculationDelayFrames    = 48
  var woundedThresholdHealth          = 40
  var evaluateDangerousBases          = false
  
  var dpsGridDistancePenalty          = 0.5
  var dpsGridMovementPenalty          = 0.5
  var dpsGridCooldownPenalty          = 0.25
  
  ///////////
  // Macro //
  ///////////
  
  var maxMineralsBeforeMinedOut = 300 * 8
  
  /////////////
  // Battles //
  /////////////
  
  var battleWorkerCostPerFrame  = 0.25
  var battleMarginTiles         = 15
  var battleMarginPixels        = battleMarginTiles * 32.0
  var battleEstimationFrames    = 24 * 2
  
  /////////////////
  // Performance //
  /////////////////
  
  var peformanceFrameMilliseconds = 20
  var performanceMinimumUnitSleep = 4
  var performanceMicroAngleStep   = 4
  var useFastGroundDistance       = true
  
  var urgencyManners        = 1
  var urgencyEconomy        = 1
  var urgencyGeography      = 1
  var urgencyPlanning       = 2
  var urgencyBattles        = 5
  var urgencyGrids          = 10
  var urgencyUnitTracking   = 10
  var urgencyMicro          = 100
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualize                           = true
  var visualizationProbabilityHappyVision = 0.1
  var visualizationProbabilityTextOnly    = 0.05
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
}
