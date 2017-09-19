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
  
  var rideItOutWinrate = 0.9
  
  ///////////
  // Micro //
  ///////////
  
  var retreatCaution                  = 1.4
  var assumedBuilderTravelSpeed       = 0.65
  var attackableRangeBuffer           = 4
  var fogPositionDuration             = 24 * 20
  var violenceFrameThreshold          = 48
  var pickupRadiusPixels              = 48 //No idea what actual value is
  var enableYolo                      = true
  var enablePathRecalculation         = true
  var enableMineralWalkInSameZone     = false // Tends to lead to stuck builders
  var pathRecalculationDelayFrames    = 48
  var woundedThresholdHealth          = 40
  var evacuateDangerousBases          = false
  
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
  var blockerMineralThreshold         = 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  var maxPlacementAge                 = 24 * 3
  var maxScarabCount                  = 3
  var enableTightBuildingPlacement    = false
  
  /////////////
  // Battles //
  /////////////
  
  var abstractBattleDistancePixels  = 32.0 * 6.0
  var battleWorkerCostPerFrame      = 0.25
  var battleMarginTiles             = 20
  var battleMarginPixels            = battleMarginTiles * 32.0
  var battleEstimationFrames        = 24 * 10
  
  /////////////////
  // Performance //
  /////////////////
  
  var foreignUnitUpdatePeriod             = 4
  var peformanceFrameMilliseconds         = 30
  var garbageCollectionThresholdMs        = 5
  var initialTaskLengthMilliseconds       = 20
  var performanceMinimumUnitSleep         = 2
  var useFastGroundDistance               = true
  
  var urgentBuildingPlacement             = true
  var urgentBuildingPlacementCutoffFrames = 24 * 60 * 15
  var urgentBuildingPlacementCooldown     = 24 * 1
  var buildingPlacementMaxTilesToCheck    = 8000
  var buildingPlacementMaxTilesToEvaluate = 300
  var buildingPlacementBatchSize          = 300
  var buildingPlacementBatchingStartFrame = 24 * 60 * 4
  var buildingPlacementMaximumQueue       = 12
  var buildingPlacementTestsPathing       = false
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
  
  var visualize                           = true
  var visualizeScreen                     = true
  var visualizeMap                        = true
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
