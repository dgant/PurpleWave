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
  var fogPositionDuration             = 24 * 20
  var violenceFrameThreshold          = 24
  var enableYolo                      = true
  var enablePathRecalculation         = true
  var enableMineralWalkInSameZone     = false // Tends to lead to stuck builders
  var pathRecalculationDelayFrames    = 48
  var woundedThresholdHealth          = 40
  var evacuateDangerousBases          = true
  
  var dpsGridDistancePenalty          = 0.5
  var dpsGridMovementPenalty          = 0.5
  var dpsGridCooldownPenalty          = 0.25
  var bunkerSafetyMargin              = Math.sqrt(16.0 * 16.0 * 2.0) + Math.sqrt(32.0 * 16.0 * 2.0) + 32.0 * (4.0 + 1.0) + /* This is the margin! */ 8.0
  
  ///////////
  // Macro //
  ///////////
  
  var maxPlacementsToEvaluate         = 1000
  var maxMineralsBeforeMinedOut       = 300 * 8
  var maxFramesToSendAdvanceBuilder   = 24 * 20
  var maxFramesToTrustBuildRequest    = 24 * 60 * 10
  var baseRadiusPixels                = 32.0 * 12.0
  var baseMergingRangePixels          = 32.0 * 12.0
  var blockerMineralThreshold         = 50
  var maxPlacementAge                 = 24 * 30
  var maxScarabCount                  = 3
  var enableTightBuildingPlacement    = false
  
  /////////////
  // Battles //
  /////////////
  
  var abstractBattleDistancePixels  = 32.0 * 6.0
  var battleWorkerCostPerFrame      = 0.25
  var battleMarginTiles             = 15
  var battleMarginPixels            = battleMarginTiles * 32.0
  var battleEstimationFrames        = 24 * 5
  
  /////////////////
  // Performance //
  /////////////////
  
  var buildingPlacementBatchSize      = 300
  var buildingPlacementMaximumQueue   = 40
  var garbageCollectionThresholdMs    = 5
  var peformanceFrameMilliseconds     = 20
  var initialTaskLengthMilliseconds   = 20
  var performanceMinimumUnitSleep     = 2
  var performanceMicroAngleStep       = 4
  var useFastGroundDistance           = true
  var verifyBuildingsDontBreakPaths   = false
  var emergencyBuildingPlacement      = true
  var emergencyBuildingCutoffFrames   = 24 * 60 * 5
  var emergencyBuildingCooldown       = 24 * 5
  var unitHistoryAge                  = 24 * 3
  var buildingPlacements              = 5
  
  var urgencyManners            = 1
  var urgencyEconomy            = 1
  var urgencyGeography          = 1
  var urgencyArchitecture       = 1
  var urgencyGrids              = 2
  var urgencyPlanning           = 3
  var urgencyBattles            = 5
  var urgencyUnitTracking       = 5
  var urgencyMicro              = 100
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualize                           = true
  var visualizationProbabilityHappyVision = 0.1
  var visualizationProbabilityTextOnly    = 0.00
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
}
