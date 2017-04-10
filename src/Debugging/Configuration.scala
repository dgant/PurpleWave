package Debugging

class Configuration {
  
  val combatEvaluationDistanceTiles   = 18
  val combatDistancePenalty           = 0.25
  val combatMovementPenalty           = 0.5
  val combatCooldownPenalty           = 0.75
  val combatStickinessLeash           = 64
  var enablePathRecalculation         = false
  var enableFastGroundDistance        = true
  
  var visualize                   = true
  var visualizeBases              = false
  var visualizeBattles            = true
  var visualizeBullets            = false
  var visualizeChokepoints        = false
  var visualizeEconomy            = false
  var visualizeGeography          = true
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
  
  var enableCamera                = true
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var viewportWidth               = 640 * 2
  var viewportHeight              = 480 * 2
}
