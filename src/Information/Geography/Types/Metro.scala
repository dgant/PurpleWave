package Information.Geography.Types

case class Metro(bases: Seq[Base]) {
  def merge(other: Metro): Metro= Metro(bases ++ other.bases)
  val main: Option[Base] = bases.find(_.isStartLocation)
  val natural: Option[Base] = main.flatMap(_.natural)
}
