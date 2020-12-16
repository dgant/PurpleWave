package Lifecycle

import Strategery.{DefaultPlaybook, Playbook}
import Utilities.GameTime

class Configuration {

  /////////////////////////
  // Tournament settings //s
  /////////////////////////

  var enableSurrenders        = false
  var enableChat              = false
  var frameMillisecondTarget  = 20
  var frameMillisecondLimit   = 55

  ///////////////////
  // Mode settings //
  ///////////////////

  var humanMode                       = false
  var visualizeFun                    = false
  var visualizeDebug                  = false
  var debugging                       = false
  var logstd                          = false
  var debugPauseThreshold: Int        = 24 * 60 * 60

  //////////////
  // Strategy //
  //////////////

  var dynamicStickiness   = 15.0
  var targetWinrate       = 0.8
  var historyHalfLife     = 3.0
  var recentFingerprints  = 2
  var forcedStrategy: Option[String] = None
  var forcedPlaybook: Option[Playbook] = None
  def playbook: Playbook = forcedPlaybook.getOrElse(DefaultPlaybook)
  
  /////////////
  // Battles //
  /////////////

  var enableMCRS                    = false
  var avatarBattleDistancePixels    = 32.0 * 6.0
  var battleMarginTileMinimum       = 6
  var battleMarginTileMaximum       = 12 * 2 + 2 // A bit over double Siege Tank range
  var battleHysteresisFrames        = GameTime(0, 6)()
  var baseThreshold                    = 0.04
  var simulationFrames              = GameTime(0, 12)()
  var simulationEstimationPeriod    = 6
  var simulationBonusTankRange      = 64.0
  var simulationDamageValueRatio    = 0.1
  
  ///////////
  // Micro //
  ///////////

  var violenceThresholdFrames         = GameTime(0, 2)()
  
  ///////////
  // Macro //
  ///////////
  
  var minimumMineralsBeforeMinedOut   = 150 * 8
  var maxFramesToSendAdvanceBuilder   = GameTime(0, 40)()
  var blockerMineralThreshold         = 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  var enableTightBuildingPlacement    = false
  
  /////////////////
  // Performance //
  /////////////////

  var enablePerformancePauses             = true
  var foreignUnitUpdatePeriod             = 1
  var performanceMinimumUnitSleep         = 2
  var maximumGamesHistoryPerOpponent      = 500
  var logTaskDuration                     = false
  
  var buildingPlacementRefreshPeriod      = GameTime(0, 3)()
  var buildingPlacementMaxTilesToEvaluate = 300
  var buildingPlacementMaximumQueue       = 12
  
  var urgencyManners            = 1
  var urgencyEconomy            = 1
  var urgencyGather             = 1
  var urgencyGeography          = 1
  var urgencyArchitecture       = 1
  var urgencyGrids              = 2
  var urgencyPlanning           = 5
  var urgencySquads             = 20
  var urgencyBattles            = 20
  var urgencyUnitTracking       = 20
  var urgencyMicro              = 50
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualizeScreen                     = true
  var visualizeMap                        = true
  var visualizationProbabilityHappyVision = 0.0 // 0.05
  var visualizationProbabilityTextOnly    = 0.0 // 0.01
  var visualizationCullViewport           = true

  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight

  ///////////////
  // Debugging //
  ///////////////
  var trackUnit = false
}
