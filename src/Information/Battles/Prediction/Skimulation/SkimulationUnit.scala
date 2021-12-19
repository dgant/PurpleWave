package Information.Battles.Prediction.Skimulation

trait SkimulationUnit {
  var skimStrength: Double = 0.0
  var skimMagic: Double = 0.0
  var skimEngaged: Boolean = false
  var skimEnagingSoon: Boolean = false
  var skimFront: Boolean = false
  var skimFrontSoon: Boolean = false
  var skimDistanceToEngage: Double = 0.0
}
