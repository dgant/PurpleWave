package Planning.Composition.UnitCounters

class UnitCountExactly(var originalQuantity:Int = 1) extends UnitCountBetween {
  
  minimum.set(originalQuantity)
  maximum.set(originalQuantity)
}
