package Types.Requirements

trait PriorityMultiplier {
  def multiplier():Integer
}

object PriorityMinimum extends PriorityMultiplier {
  override def multiplier():Integer = 1
}

object PriorityOptimal extends PriorityMultiplier {
  override def multiplier():Integer = 1000
}

object PriorityOptional extends PriorityMultiplier {
  override def multiplier():Integer = 1000 * 1000
}
