package Information.Battles.Estimations

case class ReportCard(
  valueDealt      : Double,
  valueReceived   : Double,
  damageDealt     : Double,
  damageReceived  : Double,
  dead            : Boolean,
  killed          : Int)
