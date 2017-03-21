package Debugging

class Configuration {
  var gameSpeed                         = 0
  
  var enableStdOut                      = false
  var enableChat                        = true
  var enableLatencyCompensation         = false
  
  var enableCamera                      = false
  var cameraDynamicSpeed                = false
  var cameraDynamicSpeedSlowest         = 30
  var cameraDynamicSpeedFastest         = 0
  
  var enableVisualization               = true
  var enableVisualizationBases          = false
  var enableVisualizationBattles        = false
  var enableVisualizationBullets        = true
  var enableVisualizationChokepoints    = false
  var enableVisualizationEconomy        = false
  var enableVisualizationGrids          = false
  var enableVisualizationPerformance    = false
  var enableVisualizationPlans          = false
  var enableVisualizationResources      = false
  var enableVisualizationScheduler      = false
  var enableVisualizationGeography      = true
  var enableVisualizationTextOnly       = false
  var enableVisualizationUnitsForeign   = false
  var enableVisualizationUnitsOurs      = false
  var enableVisualizationVectorUnits    = true
  var enableVisualizationZones          = false
  
  var enableGoonStopProtection          = true
}
