package Debugging

class Configuration {
  
  ///////////////
  // Operation //
  ///////////////
  
  var gameSpeed           = 0
  var enableSurrendering  = true
  var enableStdOut        = false // StdOut crashes bots on SSCAIT because it blocks forever
  var enableChat          = true
  var identifyGhostUnits  = true
  
  //////////////
  // Strategy //
  //////////////
  
  var useIteratedRoundRobinStrategySelection  = false
  var rideItOutWinrate                        = 0.9
  
  
  ///////////
  // Micro //
  ///////////
  
  var assumedBuilderTravelSpeed       = 0.65
  var attackableRangeBuffer           = 4
  var fogPositionDuration             = 24 * 20
  var violenceFrameThreshold          = 24
  var enableYolo                      = true
  var enablePathRecalculation         = true
  var enableMineralWalkInSameZone     = false // Tends to lead to stuck builders
  var pathRecalculationDelayFrames    = 48
  var woundedThresholdHealth          = 40
  var evacuateDangerousBases          = true
  
  var dpfGridDistancePenalty          = 0.5
  var dpfGridMovementPenalty          = 0.5
  var dpfGridCooldownPenalty          = 0.25
  var bunkerSafetyMargin              = Math.sqrt(16.0 * 16.0 * 2.0) + Math.sqrt(32.0 * 16.0 * 2.0) + 32.0 * (4.0 + 1.0) + /* This is the margin! */ 8.0
  
  ///////////
  // Macro //
  ///////////
  
  var maxMineralsBeforeMinedOut       = 300 * 8
  var maxFramesToSendAdvanceBuilder   = 24 * 40
  var maxFramesToTrustBuildRequest    = 24 * 60 * 10
  var baseRadiusPixels                = 32.0 * 15.0
  var baseMergingRangePixels          = 32.0 * 12.0
  var blockerMineralThreshold         = 50
  var maxPlacementAge                 = 24 * 60
  var maxScarabCount                  = 3
  var enableTightBuildingPlacement    = false
  
  /////////////
  // Battles //
  /////////////
  
  var abstractBattleDistancePixels  = 32.0 * 6.0
  var battleWorkerCostPerFrame      = 0.25
  var battleMarginTiles             = 18
  var battleMarginPixels            = battleMarginTiles * 32.0
  var battleEstimationFrames        = 24 * 30
  
  /////////////////
  // Performance //
  /////////////////
  
  var buildingPlacementMaxTilesToEvaluate = 1000
  var buildingPlacementBatchSize          = 300
  var buildingPlacementBatchingStartFrame = 24 * 60 * 4
  var buildingPlacementMaximumQueue       = 40
  var garbageCollectionThresholdMs        = 5
  var peformanceFrameMilliseconds         = 20
  var initialTaskLengthMilliseconds       = 20
  var performanceMinimumUnitSleep         = 2
  var performanceMicroAngleStep           = 4
  var useFastGroundDistance               = true
  var verifyBuildingsDontBreakPaths       = false
  var urgentBuildingPlacement             = true
  var urgentBuildingPlacementCutoffFrames = 24 * 60 * 15
  var urgentBuildingPlacementCooldown     = 24 * 1
  var unitHistoryAge                      = 24 * 3
  
  var urgencyManners            = 1
  var urgencyEconomy            = 1
  var urgencyGeography          = 1
  var urgencyArchitecture       = 1
  var urgencyGrids              = 2
  var urgencyPlanning           = 5
  var urgencyBattles            = 20
  var urgencyUnitTracking       = 20
  var urgencyMicro              = 100
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualize                           = false
  var visualizationProbabilityHappyVision = 0.1
  var visualizationProbabilityTextOnly    = 0.00
  
  var camera                      = true
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
}
