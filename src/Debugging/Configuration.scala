package Debugging

class Configuration {
  
  val combatEvaluationDistanceTiles   = 15
  val combatDistancePenalty           = 0.25
  val combatMovementPenalty           = 0.5
  val combatCooldownPenalty           = 0.75
  val combatStickinessLeash           = 0
  var enableFastGroundDistance        = true
  var enablePathRecalculation         = true
  var pathRecalculationDelayFrames    = 240
  
  var visualize                   = true
  var visualizeBases              = false
  var visualizeBattles            = true
  var visualizeBullets            = false
  var visualizeChokepoints        = true
  var visualizeEconomy            = false
  var visualizeGeography          = false
  var visualizeGrids              = false
  var visualizeHeuristicMovement  = true
  var visualizeHeuristicTargeting = true
  var visualizePerformance        = true
  var visualizePlans              = false
  var visualizeResources          = false
  var visualizeRealEstate         = true
  var visualizeScheduler          = false
  var visualizeTextOnly           = false
  var visualizeUnitsForeign       = true
  var visualizeUnitsOurs          = true
  var visualizeVectorUnits        = false
  
  var gameSpeed                   = 0
  var enableSurrendering          = true
  var enableStdOut                = false
  var enableChat                  = true
  var enableLatencyCompensation   = false
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var viewportWidth               = 640 * 2
  var viewportHeight              = 480 * 2
  
  var maxFrameMilliseconds = 80
  
  var urgencyManners      = 1
  var urgencyEconomy      = 2
  var urgencyBattles      = 3
  var urgencyGeography    = 4
  var urgencyPlanning     = 6
  var urgencyMicro        = 7
  var urgencyGrids        = 8
  var urgencyUnitTracking = 10
}
