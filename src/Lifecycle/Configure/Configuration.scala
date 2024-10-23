package Lifecycle.Configure

import Strategery.{DefaultPlaybook, Playbook}
import Utilities.Time.Seconds

class Configuration {

  /////////////////////////
  // Tournament settings //
  /////////////////////////

  var enableSurrenders  = false
  var enableChat        = false
  var frameTargetMs     = 20
  var frameLimitMs      = 35

  ///////////////////
  // Mode settings //
  ///////////////////

  var logstd            = false
  var humanMode         = false
  var visualizeFun      = false
  var visualizeDebug    = false
  var debugging         = false
  var detectBreakpoints = false
  var camera            = false

  //////////////
  // Strategy //
  //////////////

  var targetWinrate       = 0.75
  var historyHalfLife     = 3.0
  var recentFingerprints  = 2
  var fixedBuilds         = ""
  var forcedStrategy: Option[String] = None
  var forcedPlaybook: Option[Playbook] = None
  def playbook: Playbook = forcedPlaybook.getOrElse(DefaultPlaybook)
  
  /////////////
  // Battles //
  /////////////

  var simulationAsynchronous        = true
  var simulationFrames              = Seconds(15)()
  var simulationResolution          = 8
  var simulationDamageValueRatio    = 0.1

  /////////////////
  // Performance //
  /////////////////

  var enablePerformancePauses             = true
  var maximumGamesHistoryPerOpponent      = 1000
  var logTaskDuration                     = false
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualizeScreen                     = true
  var visualizeMap                        = true
  var visualizationProbabilityHappyVision = 0.0 // 0.05
  var visualizationProbabilityTextOnly    = 0.0 // 0.01

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
