package Debugging

import Information.Intelligenze.Fingerprinting.Generic.GameTime

class Configuration {
  
  /////////////////////////
  // Tournament Settings //
  /////////////////////////
  
  var enableSurrenders            = true
  var enablePerformanceStops      = true
  var enablePerformanceSurrender  = false
  var enableChat                  = true
  var identifyGhostUnits          = true
  
  //////////////
  // Strategy //
  //////////////
  
  var targetWinrate       = 0.7
  var strategyRandomness  = 0.1
  var historyHalfLife     = 50.0
  
  /////////////
  // Battles //
  /////////////
  
  var avatarBattleDistancePixels    = 32.0 * 6.0
  var battleMarginTileBase          = 12 + 2
  var battleMarginTileMinimum       = 12 + 2
  var battleMarginTileMaximum       = 12 * 2 + 2 // A bit over double Siege Tank range
  var simulationFrames        = GameTime(0, 8)()
  var battleHysteresisFrames        = GameTime(0, 6)()
  var battleHysteresisRatio         = 0.15
  var battleValueTarget             = 0.55
  var simulationBonusTankRange      = 64.0
  var simulationRetreatDelay        = 8
  var simulationDamageValueRatio    = 0.1
  
  ///////////
  // Micro //
  ///////////
  
  var concaveMarginPixels             = 16.0
  var assumedBuilderTravelSpeed       = 0.65
  var fogPositionDurationFrames       = GameTime(0, 20)()
  var violenceThresholdFrames         = GameTime(0, 2)()
  var pickupRadiusPixels              = 48 //No idea what actual value is
  var enablePathRecalculation         = true
  var workerDefenseRadiusPixels       = 32.0 * 4.0
  
  ///////////
  // Macro //
  ///////////
  
  var maxMineralsBeforeMinedOut       = 300 * 8
  var maxFramesToSendAdvanceBuilder   = GameTime(0, 40)()
  var maxFramesToTrustBuildRequest    = GameTime(10, 0)()
  var blockerMineralThreshold         = 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  var maxPlacementAgeFrames           = GameTime(0, 3)()
  var enableTightBuildingPlacement    = false
  
  /////////////////
  // Performance //
  /////////////////
  
  var foreignUnitUpdatePeriod             = 4
  var garbageCollectionThresholdMs        = 5
  var initialTaskLengthMilliseconds       = 20
  var performanceMinimumUnitSleep         = 2
  var maximumGamesHistoryPerOpponent      = 500
  
  var urgentBuildingPlacement             = true
  var urgentBuildingPlacementCutoffFrames = GameTime(15, 0)()
  var urgentBuildingPlacementCooldown     = GameTime(0, 1)()
  var buildingPlacementMaxTilesToEvaluate = 300
  var buildingPlacementBatchSize          = 300
  var buildingPlacementBatchingStartFrame = GameTime(4, 0)()
  var buildingPlacementMaximumQueue       = 12
  var buildingPlacementTestsPathing       = false
  
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
  var visualizationProbabilityHappyVision = 0.05
  var visualizationProbabilityTextOnly    = 0.01
  var visualizationCullViewport           = true
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight
}
