package Planning.UnitCounters

case class CountExactly(var originalQuantity: Int = 1) extends CountBetween {
  
  minimum.set(originalQuantity)
  maximum.set(originalQuantity)
}
