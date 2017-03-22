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
  
  var enableVisualization               = false
  var enableVisualizationBases          = false
  var enableVisualizationBattles        = false
  var enableVisualizationBullets        = false
  var enableVisualizationChokepoints    = false
  var enableVisualizationEconomy        = false
  var enableVisualizationGrids          = false
  var enableVisualizationPerformance    = true
  var enableVisualizationPlans          = false
  var enableVisualizationResources      = false
  var enableVisualizationScheduler      = false
  var enableVisualizationGeography      = false
  var enableVisualizationTextOnly       = false
  var enableVisualizationUnitsForeign   = false
  var enableVisualizationUnitsOurs      = false
  var enableVisualizationVectorUnits    = false
  var enableVisualizationZones          = false
  
  var enableGoonStopProtection          = true
  
  if (enableVisualizationVectorUnits) {
    enableVisualization = true
    enableVisualizationBullets = true
    enableVisualizationGeography = true
  }
}
