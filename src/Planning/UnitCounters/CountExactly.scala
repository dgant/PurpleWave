package Planning.UnitCounters

case class CountExactly(override val minimum: Int) extends UnitCounter {
  override val maximum: Int = minimum
}