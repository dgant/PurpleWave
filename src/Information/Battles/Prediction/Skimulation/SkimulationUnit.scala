package Information.Battles.Prediction.Skimulation

import ProxyBwapi.UnitInfo.UnitInfo

trait SkimulationUnit {
  var skimDistanceToEngage  : Double = _
  var skimStrength          : Double = _
  var skimMagic             : Double = _
  var skimDelay             : Double = _
  var skimExtension         : Double = _
  var skimPresence          : Double = _
  // Only for debug/display purposes
  var skimStrengthDisplay   : Double = _
  var skimPresenceDisplay   : Double = _
  var skimTarget            : Option[UnitInfo] = None
}
