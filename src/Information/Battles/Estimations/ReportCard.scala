package Information.Battles.Estimations

case class ReportCard(
  estimation      : Estimation,
  valueDealt      : Double,
  valueReceived   : Double,
  damageDealt     : Double,
  damageReceived  : Double,
  dead            : Boolean,
  killed          : Int) {
  
  lazy val netValuePerFrame: Double = (valueDealt - valueReceived) / Math.max(1, estimation.frames)
}
