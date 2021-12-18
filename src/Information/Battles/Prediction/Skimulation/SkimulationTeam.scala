package Information.Battles.Prediction.Skimulation

import ProxyBwapi.UnitInfo.UnitInfo

trait SkimulationTeam {
  var skimUnitsClosest: Vector[UnitInfo] = Vector.empty
  var skimFrontStart: Int = 0
  var skimFrontEnd: Int = 0
  var skimStrengthTotal: Double = 0
  var skimStrengthAir: Double = 0
  var skimStrengthGround: Double = 0
  var skimStrengthVsAir: Double = 0
  var skimStrengthVsGround: Double = 0
}
